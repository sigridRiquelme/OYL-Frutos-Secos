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

internal enum class EstadoPedidoRemoto(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    EN_PREPARACION("En preparación"),
    LISTO_PARA_RETIRO("Listo para retiro"),
    EN_DESPACHO("En despacho"),
    ENTREGADO("Entregado"),
    CANCELADO("Cancelado")
}

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

    suspend fun listarParaAdministracion(sesion: SesionRemota): List<Pedido> =
        api.listar("Bearer ${sesion.token}").map { it.paraGestion() }

    suspend fun cambiarEstado(sesion: SesionRemota, numero: String, estado: EstadoPedidoRemoto): Pedido =
        api.cambiarEstado("Bearer ${sesion.token}", numero, CambioEstado(estado)).paraGestion()

    private data class CambioEstado(val estado: EstadoPedidoRemoto)

    private data class PedidoAdministrativo(
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
        fun paraGestion() = Pedido(
            numero = numero,
            fecha = fecha,
            clienteId = null,
            nombreCliente = nombreCliente,
            telefono = telefono,
            modalidad = if (modalidad == ModalidadCompra.DESPACHO) "Despacho" else "Retiro",
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
        suspend fun historial(@Header("Authorization") token: String): List<PedidoRemoto>

        @GET("api/pedidos")
        suspend fun listar(@Header("Authorization") token: String): List<PedidoAdministrativo>

        @PATCH("api/pedidos/{numero}/estado")
        suspend fun cambiarEstado(
            @Header("Authorization") token: String,
            @Path("numero") numero: String,
            @Body datos: CambioEstado
        ): PedidoAdministrativo
    }
}
