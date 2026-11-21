package com.example.oylfrutossecos.data.repository

import com.example.oylfrutossecos.data.model.DetallePedido
import com.example.oylfrutossecos.data.model.Pedido
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.Path

internal data class ItemCompra(val idProducto: Int, val cantidad: Int)
internal enum class ModalidadCompra(val etiqueta: String) { RETIRO("Retiro"), DESPACHO("Despacho") }
internal data class SolicitudCompra(
    val solicitudId: String,
    val nombreCliente: String,
    val telefono: String,
    val modalidad: ModalidadCompra,
    val direccion: String?,
    val productos: List<ItemCompra>
)

internal data class PedidoRemoto(
    val numero: String,
    val total: Int,
    val estado: EstadoPedidoRemoto,
    val modalidad: ModalidadCompra
)

internal enum class EstadoPedidoRemoto(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    EN_PREPARACION("En preparación"),
    LISTO_PARA_RETIRO("Listo para retiro"),
    EN_DESPACHO("En despacho"),
    ENTREGADO("Entregado"),
    CANCELADO("Cancelado")
}

internal class PedidosRemotos(host: String, puerto: Int = 8084) {
    private val api = Retrofit.Builder()
        .baseUrl("http://$host:$puerto/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PedidosApi::class.java)

    suspend fun comprar(sesion: SesionRemota, solicitud: SolicitudCompra): PedidoRemoto =
        api.comprar("Bearer ${sesion.token}", solicitud)

    suspend fun historial(sesion: SesionRemota): List<Pedido> =
        api.historial("Bearer ${sesion.token}").map { it.aPedido() }

    suspend fun listarParaAdministracion(sesion: SesionRemota): List<Pedido> =
        api.listar("Bearer ${sesion.token}").map { it.aPedido() }

    suspend fun cambiarEstado(sesion: SesionRemota, numero: String, estado: EstadoPedidoRemoto): Pedido =
        api.cambiarEstado("Bearer ${sesion.token}", numero, CambioEstado(estado)).aPedido()

    private data class CambioEstado(val estado: EstadoPedidoRemoto)

    private data class RespuestaPedido(
        val numero: String,
        val fecha: Long,
        val nombreCliente: String,
        val telefono: String,
        val modalidad: ModalidadCompra,
        val direccion: String?,
        val productos: List<DetallePedido>,
        val total: Int,
        val estado: EstadoPedidoRemoto
    ) {
        fun aPedido() = Pedido(
            numero = numero,
            fecha = fecha,
            clienteId = null,
            nombreCliente = nombreCliente,
            telefono = telefono,
            modalidad = modalidad.etiqueta,
            direccion = direccion.orEmpty(),
            productos = productos,
            total = total,
            estado = estado.etiqueta
        )
    }

    private interface PedidosApi {
        @POST("api/pedidos")
        suspend fun comprar(
            @Header("Authorization") token: String,
            @Body solicitud: SolicitudCompra
        ): PedidoRemoto

        @GET("api/pedidos/mis-pedidos")
        suspend fun historial(@Header("Authorization") token: String): List<RespuestaPedido>

        @GET("api/pedidos")
        suspend fun listar(@Header("Authorization") token: String): List<RespuestaPedido>

        @PATCH("api/pedidos/{numero}/estado")
        suspend fun cambiarEstado(
            @Header("Authorization") token: String,
            @Path("numero") numero: String,
            @Body datos: CambioEstado
        ): RespuestaPedido
    }
}
