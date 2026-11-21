package com.example.oylfrutossecos

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.oylfrutossecos.ui.screens.precioOYL
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginBackendTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun iniciaYTerminaSesionConUnaCuentaDelBackend() {
        iniciarSesion()
        compose.onNodeWithText("Cerrar sesión").performClick()
        compose.onNodeWithText("Iniciar sesión").assertIsDisplayed()
    }

    @Test
    fun compraDesdeElCarritoYConsultaElHistorial() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("oylProbarCompra") == "true")
        iniciarSesion()
        esperarTexto("Almendras naturales")
        compose.onNodeWithText("Almendras naturales").performClick()
        compose.onNodeWithText("Agregar al carrito").performScrollTo().performClick()
        compose.onNodeWithText("Carrito (1)").performClick()
        esperarTexto("Continuar al pedido")
        compose.onNodeWithText("Continuar al pedido").performClick()
        compose.onNode(hasSetTextAction() and hasText("Teléfono de contacto"))
            .performScrollTo().performTextInput("+56912345678")
        compose.onNode(hasClickAction() and hasText("Confirmar pedido"))
            .performScrollTo().performClick()
        esperarTexto("Pedido confirmado")
        compose.onNodeWithText("Total: ${precioOYL(4500)}").assertIsDisplayed()
        compose.onNodeWithText("Ver mis pedidos").performClick()
        esperarTexto("Historial de pedidos")
        compose.onNodeWithText("PENDIENTE · ${precioOYL(4500)}").assertIsDisplayed()
    }

    private fun iniciarSesion() {
        val argumentos = InstrumentationRegistry.getArguments()
        val usuario = argumentos.getString("oylUsuarioPrueba").orEmpty()
        val clave = argumentos.getString("oylClavePrueba").orEmpty()
        val nombre = argumentos.getString("oylNombrePrueba").orEmpty()
        assumeTrue(usuario.isNotBlank() && clave.isNotBlank() && nombre.isNotBlank())

        compose.onNodeWithText("Comenzar").performClick()
        compose.onNode(hasSetTextAction() and hasText("Nombre de usuario")).performTextInput(usuario)
        compose.onNode(hasSetTextAction() and hasText("Contraseña")).performTextInput(clave)
        compose.onNodeWithText("Iniciar sesión").performScrollTo().performClick()
        esperarTexto(nombre)
        compose.onNodeWithText(nombre).assertIsDisplayed()
    }

    private fun esperarTexto(texto: String) {
        compose.waitUntil(25_000) {
            compose.onAllNodesWithText(texto).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
