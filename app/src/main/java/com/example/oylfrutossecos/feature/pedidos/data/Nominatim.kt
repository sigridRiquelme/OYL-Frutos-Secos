package com.example.oylfrutossecos.feature.pedidos.data

import android.content.Context
import android.content.RestrictionsManager
import android.os.SystemClock
import com.example.oylfrutossecos.BuildConfig
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.MalformedJsonException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import java.io.IOException
import java.net.ProtocolException
import java.util.concurrent.TimeUnit

internal fun esDespachoConDireccion(modalidad: String, direccion: String): Boolean =
    modalidad.trim().equals("Despacho", ignoreCase = true) && direccion.isNotBlank()

// Solo se envían consultas canónicas de lugares públicos, nunca texto libre del pedido.
internal enum class DireccionDemo(val consulta: String) {
    MONEDA("Palacio de La Moneda, Santiago, Chile"),
    MUSEO("Museo Nacional de Bellas Artes, Santiago, Chile");

    companion object {
        fun desde(direccion: String): DireccionDemo? =
            entries.firstOrNull { it.consulta.equals(direccion.trim(), ignoreCase = true) }
    }
}

internal data class CoincidenciaNominatim(
    val lat: String?,
    val lon: String?,
    @SerializedName("display_name") val direccion: String?,
    @SerializedName("addresstype") val tipo: String?
)

internal interface NominatimApi {
    @Headers("User-Agent: OYL-Frutos-Secos/1.0 (https://github.com/sigridRiquelme/OYL-Frutos-Secos)")
    @GET("search?format=jsonv2&limit=1&countrycodes=cl&accept-language=es")
    suspend fun buscar(@Query("q") direccion: String): Response<List<CoincidenciaNominatim?>>
}

internal sealed interface UbicacionDespacho {
    data object Cargando : UbicacionDespacho
    data object NoEncontrada : UbicacionDespacho
    data object Ambigua : UbicacionDespacho
    data object SinConexion : UbicacionDespacho
    data object RespuestaInvalida : UbicacionDespacho
    data object Deshabilitada : UbicacionDespacho
    data class ErrorHttp(val codigo: Int) : UbicacionDespacho
    data class Encontrada(val latitud: Double, val longitud: Double, val direccion: String) :
        UbicacionDespacho
}

internal fun interpretarUbicacion(resultados: List<CoincidenciaNominatim?>): UbicacionDespacho {
    if (resultados.isEmpty()) return UbicacionDespacho.NoEncontrada
    if (resultados.size > 1) return UbicacionDespacho.Ambigua
    val lugar = resultados.single() ?: return UbicacionDespacho.RespuestaInvalida
    val latitud = lugar.lat?.toDoubleOrNull() ?: return UbicacionDespacho.RespuestaInvalida
    val longitud = lugar.lon?.toDoubleOrNull() ?: return UbicacionDespacho.RespuestaInvalida
    val direccion = lugar.direccion?.takeIf { it.isNotBlank() }
        ?: return UbicacionDespacho.RespuestaInvalida
    if (latitud !in -90.0..90.0 || longitud !in -180.0..180.0) {
        return UbicacionDespacho.RespuestaInvalida
    }
    // Una calle, comuna o región podría coincidir sin identificar el lugar solicitado.
    if (lugar.tipo == null || lugar.tipo in tiposImprecisos ||
        !direccion.contains("Santiago", ignoreCase = true)
    ) {
        return UbicacionDespacho.Ambigua
    }
    return UbicacionDespacho.Encontrada(latitud, longitud, direccion)
}

private val tiposImprecisos = setOf(
    "road", "street", "pedestrian", "residential", "neighbourhood", "suburb",
    "quarter", "city_district", "municipality", "city", "town", "village",
    "county", "state", "region", "country", "postcode", "administrative"
)

internal fun crearNominatimApi(baseUrl: String): NominatimApi = Retrofit.Builder()
    .baseUrl(baseUrl)
    .client(
        OkHttpClient.Builder()
            .retryOnConnectionFailure(false)
            .followRedirects(false)
            .followSslRedirects(false)
            .addNetworkInterceptor { cadena ->
                val respuesta = cadena.proceed(cadena.request())
                // OkHttp puede repetir un 503 con Retry-After: 0 aun con los reintentos desactivados.
                if (respuesta.code == 503 && respuesta.header("Retry-After") == "0") {
                    respuesta.newBuilder().removeHeader("Retry-After").build()
                } else {
                    respuesta
                }
            }
            .callTimeout(12, TimeUnit.SECONDS)
            .build()
    )
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(NominatimApi::class.java)

internal suspend fun consultarUbicacion(
    api: NominatimApi,
    direccion: DireccionDemo,
    guardar: (List<CoincidenciaNominatim?>) -> Unit
): UbicacionDespacho = try {
    val respuesta = api.buscar(direccion.consulta)
    if (respuesta.isSuccessful) {
        val resultados = respuesta.body()
        if (resultados == null) {
            UbicacionDespacho.RespuestaInvalida
        } else {
            interpretarUbicacion(resultados).also {
                if (it != UbicacionDespacho.RespuestaInvalida) guardar(resultados)
            }
        }
    } else {
        respuesta.errorBody()?.close()
        UbicacionDespacho.ErrorHttp(respuesta.code())
    }
} catch (_: JsonParseException) {
    UbicacionDespacho.RespuestaInvalida
} catch (_: MalformedJsonException) {
    UbicacionDespacho.RespuestaInvalida
} catch (_: ProtocolException) {
    UbicacionDespacho.RespuestaInvalida
} catch (_: IOException) {
    UbicacionDespacho.SinConexion
}

internal object GeocodificadorDespacho {
    private const val BASE_PUBLICA = "https://nominatim.openstreetmap.org/"
    private val acceso = Mutex()
    private val gson = Gson()
    private val tipoResultados = object : TypeToken<List<CoincidenciaNominatim?>>() {}.type
    private var siguienteSolicitud = 0L

    suspend fun buscar(contexto: Context, direccion: DireccionDemo): UbicacionDespacho =
        withContext(Dispatchers.IO) {
            acceso.withLock {
                val base = endpoint(contexto) ?: return@withLock UbicacionDespacho.Deshabilitada
                val cache = contexto.getSharedPreferences("oyl_geocodificacion_demo", Context.MODE_PRIVATE)
                val clave = "$base|${direccion.name}"
                val guardado = cache.getString(clave, null)?.let {
                    try {
                        gson.fromJson<List<CoincidenciaNominatim?>>(it, tipoResultados)
                    } catch (_: JsonParseException) {
                        null
                    }
                }
                if (guardado != null) return@withLock interpretarUbicacion(guardado)

                delay((siguienteSolicitud - SystemClock.elapsedRealtime()).coerceAtLeast(0))
                try {
                    consultarUbicacion(crearNominatimApi(base), direccion) {
                        cache.edit().putString(clave, gson.toJson(it)).apply()
                    }
                } finally {
                    siguienteSolicitud = SystemClock.elapsedRealtime() + 1_100
                }
            }
        }

    private fun endpoint(contexto: Context): String? {
        if (!BuildConfig.GEOCODIFICACION_DEMO) return null
        val restricciones = contexto.getSystemService(RestrictionsManager::class.java)
            ?.applicationRestrictions
        val valor = restricciones?.getString("nominatim_endpoint") ?: BASE_PUBLICA
        val url = valor.toHttpUrlOrNull() ?: return null
        if (!url.isHttps || url.username.isNotEmpty() || url.password.isNotEmpty() ||
            url.query != null || url.fragment != null
        ) return null
        val base = url.toString()
        return if (base.endsWith("/")) base else "$base/"
    }
}
