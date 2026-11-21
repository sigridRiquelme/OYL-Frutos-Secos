package com.example.oylfrutossecos.data.repository

import com.example.oylfrutossecos.data.model.ProductoCatalogo
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import com.example.oylfrutossecos.feature.productos.domain.DatosNuevoProducto

class CatalogoRemoto(host: String) {
    private val http = OkHttpClient.Builder().retryOnConnectionFailure(false).build()
    private val baseFotos = "http://$host:8081"
    private val gson = Gson()
    private val productos = cliente(host, 8081).create(ProductosApi::class.java)
    private val inventario = cliente(host, 8082).create(InventarioApi::class.java)

    suspend fun cargar(): List<ProductoCatalogo> {
        val catalogo = productos.listar()
        val stock = inventario.listar().associate { it.productoId to it.stockDisponible }
        return catalogo.map { producto -> producto.alCatalogo(stock[producto.id] ?: 0) }
    }

    suspend fun crear(sesion: SesionRemota, datos: DatosNuevoProducto, clave: String,
                      foto: ByteArray?): ProductoCatalogo {
        val creado = productos.crear("Bearer ${sesion.token}", clave, formulario(datos, foto, crear = true))
        return creado.alCatalogo(inventario.consultar(creado.id).stockDisponible)
    }

    suspend fun editar(sesion: SesionRemota, id: Int, datos: DatosNuevoProducto, stock: Int,
                       foto: ByteArray?): ProductoCatalogo =
        productos.editar("Bearer ${sesion.token}", id, formulario(datos, foto)).alCatalogo(stock)

    private fun formulario(datos: DatosNuevoProducto, foto: ByteArray?, crear: Boolean = false): RequestBody {
        val imagen = datos.imagenUri?.takeUnless { it.startsWith("content:") || it.startsWith("file:") }
            ?.let { url ->
                val ruta = url.removePrefix(baseFotos)
                if (ruta.matches(rutaFoto)) ruta else url
            }
        val producto = EditarProducto(
            datos.nombre, datos.categoria, datos.precio.toInt(), datos.descripcion,
            datos.presentacion, imagen
        )
        return MultipartBody.Builder().setType(MultipartBody.FORM).apply {
            addPart(
                okhttp3.Headers.headersOf("Content-Disposition", "form-data; name=\"datos\""),
                gson.toJson(producto).toRequestBody("application/json".toMediaType())
            )
            if (crear) addFormDataPart("stock", datos.stock)
            if (foto != null) {
                addFormDataPart("foto", "producto.jpg", foto.toRequestBody("image/jpeg".toMediaType()))
            }
        }.build()
    }

    private val rutaFoto = Regex("/api/productos/[0-9]+/foto/[a-f0-9]{64}")

    suspend fun cambiarEstado(sesion: SesionRemota, id: Int, activo: Boolean, stock: Int): ProductoCatalogo =
        productos.cambiarEstado("Bearer ${sesion.token}", id, EstadoProducto(activo)).alCatalogo(stock)

    suspend fun ajustarStock(sesion: SesionRemota, id: Int, cantidad: Int, entrada: Boolean): Int =
        inventario.ajustar(
            "Bearer ${sesion.token}", id, AjusteStock(if (entrada) "ENTRADA" else "SALIDA", cantidad)
        ).stockDisponible

    private fun ProductoRespuesta.alCatalogo(stock: Int): ProductoCatalogo =
        ProductoCatalogo(
            id = id, nombre = nombre, categoria = categoria, precio = precio, stock = stock,
            descripcion = descripcion.orEmpty(), presentacion = presentacion.orEmpty(),
            imagenUri = imagenUri?.let { if (it.matches(rutaFoto)) baseFotos + it else it }, activo = activo
        )

    private fun cliente(host: String, puerto: Int): Retrofit = Retrofit.Builder()
        .baseUrl("http://$host:$puerto/")
        .client(http)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private interface ProductosApi {
        @GET("api/productos")
        suspend fun listar(): List<ProductoRespuesta>

        @POST("api/productos")
        suspend fun crear(@Header("Authorization") token: String,
                          @Header("Idempotency-Key") clave: String,
                          @Body datos: RequestBody): ProductoRespuesta

        @PUT("api/productos/{id}")
        suspend fun editar(@Header("Authorization") token: String, @Path("id") id: Int,
                           @Body datos: RequestBody): ProductoRespuesta

        @PATCH("api/productos/{id}/estado")
        suspend fun cambiarEstado(@Header("Authorization") token: String, @Path("id") id: Int,
                                  @Body datos: EstadoProducto): ProductoRespuesta
    }

    private interface InventarioApi {
        @GET("api/inventario")
        suspend fun listar(): List<InventarioRespuesta>

        @GET("api/inventario/{id}")
        suspend fun consultar(@Path("id") id: Int): InventarioRespuesta

        @POST("api/inventario/{id}/ajustes")
        suspend fun ajustar(@Header("Authorization") token: String, @Path("id") id: Int,
                            @Body datos: AjusteStock): InventarioRespuesta
    }

    private data class ProductoRespuesta(
        val id: Int,
        val nombre: String,
        val categoria: String,
        val precio: Int,
        val descripcion: String?,
        val presentacion: String?,
        val imagenUri: String?,
        val activo: Boolean
    )

    private data class InventarioRespuesta(val productoId: Int, val stockDisponible: Int)
    private data class EstadoProducto(val activo: Boolean)
    private data class AjusteStock(val tipo: String, val cantidad: Int)
    private data class EditarProducto(
        val nombre: String, val categoria: String, val precio: Int,
        val descripcion: String, val presentacion: String, val imagenUri: String?
    )
}
