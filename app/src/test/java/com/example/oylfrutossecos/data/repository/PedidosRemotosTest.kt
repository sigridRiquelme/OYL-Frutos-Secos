package com.example.oylfrutossecos.data.repository

import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import retrofit2.HttpException

class PedidosRemotosTest {
    private lateinit var servidor: ServidorLocal
    private lateinit var origen: PedidosRemotos
    private val sesion = SesionRemota(1, "Cliente", RolRemoto.CLIENTE, "jwt-prueba", 900)
    private val respuesta = """{"numero":"OYL-P-1","fecha":1000,"nombreCliente":"Cliente","telefono":"912345678","modalidad":"RETIRO","direccion":null,"productos":[{"idProducto":1,"nombre":"Nueces","cantidad":2,"precioUnitario":2500}],"total":5000,"estado":"PENDIENTE"}"""
    @Before fun preparar() { servidor = ServidorLocal(); origen = PedidosRemotos("127.0.0.1", servidor.puerto) }
    @After fun cerrar() { servidor.close() }

    @Test fun compraSerializaProductosYModalidadConAutorizacion() = runBlocking {
        val solicitud = SolicitudCompra("clave-estable", "Cliente", "912345678", ModalidadCompra.DESPACHO, "Santiago", listOf(ItemCompra(1, 2)))
        servidor.responder(respuesta)
        val pedido = origen.comprar(sesion, solicitud)
        assertEquals(5000, pedido.total); assertEquals("OYL-P-1", pedido.numero)
        assertEquals(EstadoPedidoRemoto.PENDIENTE, pedido.estado); assertEquals(ModalidadCompra.RETIRO, pedido.modalidad)
        val peticion = servidor.recibir()
        assertEquals("POST", peticion.metodo); assertEquals("/api/pedidos", peticion.ruta)
        assertEquals("Bearer jwt-prueba", peticion.token)
        val json = JsonParser.parseString(peticion.cuerpo).asJsonObject
        assertEquals("clave-estable", json["solicitudId"].asString)
        assertEquals("DESPACHO", json["modalidad"].asString)
        assertEquals("Santiago", json["direccion"].asString)
        assertEquals(2, json["productos"].asJsonArray[0].asJsonObject["cantidad"].asInt)
    }

    @Test fun historialYAdministracionMapeanCamposSinPerderDetalles() = runBlocking {
        servidor.responder("[$respuesta]")
        val pedido = origen.historial(sesion).single()
        assertEquals("", pedido.direccion); assertNull(pedido.clienteId)
        assertEquals("Retiro", pedido.modalidad); assertEquals("Pendiente", pedido.estado)
        assertEquals(1000L, pedido.fecha); assertEquals("Cliente", pedido.nombreCliente)
        assertEquals("912345678", pedido.telefono)
        assertEquals(2500, pedido.productos.single().precioUnitario)
        assertEquals("/api/pedidos/mis-pedidos", servidor.recibir().ruta)
        servidor.responder("[$respuesta]")
        assertEquals(pedido, origen.listarParaAdministracion(sesion).single())
        assertEquals("/api/pedidos", servidor.recibir().ruta)
        servidor.responder("[]"); assertTrue(origen.historial(sesion).isEmpty())
    }

    @Test fun actualizacionEnviaPatchYLeeEstadoResultante() = runBlocking {
        for (estado in EstadoPedidoRemoto.entries) {
            servidor.responder(respuesta.replace("PENDIENTE", estado.name).replace("\"direccion\":null", "\"direccion\":\"Santiago\""))
            val pedido = origen.cambiarEstado(sesion, "OYL-P-1", estado)
            assertEquals(estado.etiqueta, pedido.estado); assertEquals("Santiago", pedido.direccion)
            val peticion = servidor.recibir()
            assertEquals("PATCH", peticion.metodo); assertEquals("/api/pedidos/OYL-P-1/estado", peticion.ruta)
            assertEquals("Bearer jwt-prueba", peticion.token)
            assertEquals(estado.name, JsonParser.parseString(peticion.cuerpo).asJsonObject["estado"].asString)
        }
    }

    @Test fun propagaRechazosHttpYFallosDeConexion() {
        for (codigo in listOf(400, 401, 403, 404, 409, 422, 500, 503)) {
            servidor.responder("{}", codigo)
            assertEquals(codigo, assertThrows(HttpException::class.java) { runBlocking { origen.historial(sesion) } }.code())
        }
        servidor.close()
        assertThrows(java.io.IOException::class.java) { runBlocking { origen.historial(sesion) } }
    }
}
