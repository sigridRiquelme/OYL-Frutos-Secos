package com.example.oylfrutossecos

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import com.example.oylfrutossecos.data.model.Pedido
import com.example.oylfrutossecos.feature.pedidos.data.UbicacionDespacho
import com.example.oylfrutossecos.ui.screens.ContenidoUbicacionDespacho
import com.example.oylfrutossecos.ui.screens.GestionPedidosScreen
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

class UbicacionDespachoUiTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun muestraCargaYEstadosSinMapa() {
        val estado = mutableStateOf<UbicacionDespacho>(UbicacionDespacho.Cargando)
        compose.setContent { ContenidoUbicacionDespacho(estado.value) }
        compose.onNodeWithText("Buscando ubicación…").assertIsDisplayed()

        val casos = listOf(
            UbicacionDespacho.NoEncontrada to "No se encontró la dirección",
            UbicacionDespacho.Ambigua to "La coincidencia es imprecisa",
            UbicacionDespacho.SinConexion to "No se pudo conectar",
            UbicacionDespacho.ErrorHttp(429) to "error HTTP 429",
            UbicacionDespacho.RespuestaInvalida to "respuesta que no se pudo verificar",
            UbicacionDespacho.Deshabilitada to "geocodificación está deshabilitada"
        )
        casos.forEach { (valor, texto) ->
            compose.runOnIdle { estado.value = valor }
            compose.onNodeWithText(texto, substring = true).assertIsDisplayed()
        }
    }

    @Test
    fun despachoConDireccionPrivadaPermiteVolverSinConsultar() {
        mostrarPedido("DESPACHO", "Dirección privada de prueba 123")
        compose.onNodeWithText("Ver ubicación del despacho").performScrollTo().performClick()
        compose.onNodeWithText("La búsqueda externa solo admite", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Consultar ubicación").assertDoesNotExist()
        compose.onNodeWithText("Volver al pedido").performClick()
        compose.onNodeWithText("Detalle del pedido").assertIsDisplayed()
        compose.onNodeWithText("Cerrar").performClick()
        compose.onNodeWithText("Gestión de Pedidos").assertIsDisplayed()
    }

    @Test
    fun retiroNoMuestraAccionAunqueTengaDireccion() {
        mostrarPedido("RETIRO", "Palacio de La Moneda, Santiago, Chile")
        compose.onNodeWithText("Ver ubicación del despacho").assertDoesNotExist()
    }

    @Test
    fun despachoSinDireccionNoMuestraAccion() {
        mostrarPedido("Despacho", "")
        compose.onNodeWithText("Ver ubicación del despacho").assertDoesNotExist()
    }

    @Test
    fun demoRealMuestraMarcadorDeEntrega() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("oylProbarNominatim") == "true")
        mostrarPedido("Despacho", "Palacio de La Moneda, Santiago, Chile")
        compose.onNodeWithText("Ver ubicación del despacho").performScrollTo().performClick()
        compose.onNodeWithText("Consultar ubicación").performClick()
        compose.waitUntil(25_000) {
            compose.onAllNodesWithContentDescription("Mapa con marcador de entrega")
                .fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithContentDescription("Mapa con marcador de entrega").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Ubicación aproximada", substring = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Volver al pedido").performClick()
        compose.onNodeWithText("Detalle del pedido").assertIsDisplayed()
    }

    private fun mostrarPedido(modalidad: String, direccion: String) {
        val pedido = Pedido(
            numero = "DEMO-1",
            fecha = 0,
            nombreCliente = "Cliente demo",
            telefono = "912345678",
            modalidad = modalidad,
            direccion = direccion,
            estado = "Pendiente",
            productos = emptyList(),
            total = 0,
            clienteId = 1
        )
        compose.setContent {
            GestionPedidosScreen(listOf(pedido), onVolver = {}, onCambiarEstado = { _, _, _ -> })
        }
        compose.onNodeWithText("Ver detalles").performScrollTo().performClick()
    }
}
