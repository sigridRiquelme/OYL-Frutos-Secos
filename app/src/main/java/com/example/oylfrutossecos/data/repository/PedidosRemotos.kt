package com.example.oylfrutossecos.data.repository

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

internal data class ItemCompra(val idProducto: Int, val cantidad: Int)
internal enum class ModalidadCompra { RETIRO, DESPACHO }
internal data class SolicitudCompra(
    val solicitudId: String,
    val nombreCliente: String,
    val telefono: String,
    val modalidad: ModalidadCompra,
    val direccion: String?,
    val productos: List<ItemCompra>
)

internal data class PedidoRemoto(val numero: String, val total: Int, val estado: String)

internal class PedidosRemotos(host: String) {
    private val api = Retrofit.Builder()
        .baseUrl("http://$host:8084/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PedidosApi::class.java)

    suspend fun comprar(sesion: SesionRemota, solicitud: SolicitudCompra): PedidoRemoto =
        api.comprar("Bearer ${sesion.token}", solicitud)

    suspend fun historial(sesion: SesionRemota): List<PedidoRemoto> =
        api.historial("Bearer ${sesion.token}")

    private interface PedidosApi {
        @POST("api/pedidos")
        suspend fun comprar(
            @Header("Authorization") token: String,
            @Body solicitud: SolicitudCompra
        ): PedidoRemoto

        @GET("api/pedidos/mis-pedidos")
        suspend fun historial(@Header("Authorization") token: String): List<PedidoRemoto>
    }
}
