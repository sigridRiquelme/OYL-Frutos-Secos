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
import androidx.compose.ui.test.performTextReplacement
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
    fun guardaElPerfilYLoRecuperaAlVolverAEntrar() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("oylProbarPerfil") == "true")
        iniciarSesion()
        fun abrirPerfil() {
            compose.onNode(hasClickAction() and hasText("Mi perfil")).performScrollTo().performClick()
            esperarTexto("Editar perfil")
        }

        abrirPerfil()
        compose.onNodeWithText("Editar perfil").performScrollTo().performClick()
        val direccion = "Av. Prueba USB 123"
        compose.onNode(hasSetTextAction() and hasText("Dirección (opcional)"))
            .performScrollTo().performTextReplacement(direccion)
        compose.onNodeWithText("Guardar cambios").performScrollTo().performClick()
        esperarTexto("Perfil actualizado.")
        compose.onNodeWithText("Dirección: $direccion").assertIsDisplayed()
        compose.onNodeWithText("Cerrar sesión").performClick()

        iniciarSesion(desdeBienvenida = false)
        abrirPerfil()
        compose.onNodeWithText("Dirección: $direccion").assertIsDisplayed()
    }

    @Test
    fun registraUnaCuentaEIniciaSesionDesdeAndroid() {
        val argumentos = InstrumentationRegistry.getArguments()
        val clave = argumentos.getString("oylClavePrueba").orEmpty()
        assumeTrue(argumentos.getString("oylProbarRegistro") == "true" && clave.isNotBlank())
        val usuario = "nuevo_" + java.util.UUID.randomUUID().toString().take(8)
        val nombre = "Cliente Nuevo USB"

        compose.onNodeWithText("Comenzar").performClick()
        compose.onNodeWithText("Regístrate").performClick()
        fun completar(campo: String, valor: String) {
            compose.onNode(hasSetTextAction() and hasText(campo))
                .performScrollTo().performTextInput(valor)
        }
        completar("Nombre completo", nombre)
        completar("Nombre de usuario", usuario)
        completar("Correo electrónico", "$usuario@example.test")
        completar("Teléfono", "+56912345678")
        completar("Contraseña", clave)
        compose.onNode(hasClickAction() and hasText("Crear Cuenta"))
            .performScrollTo().performClick()
        esperarTexto("Iniciar sesión")
        completar("Nombre de usuario", usuario)
        completar("Contraseña", clave)
        compose.onNodeWithText("Iniciar sesión").performScrollTo().performClick()
        esperarTexto(nombre)
        compose.onNodeWithText("Mis pedidos").performClick()
        esperarTexto("Todavía no tienes pedidos.")
    }

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

    private fun iniciarSesion(desdeBienvenida: Boolean = true) {
        val argumentos = InstrumentationRegistry.getArguments()
        val usuario = argumentos.getString("oylUsuarioPrueba").orEmpty()
        val clave = argumentos.getString("oylClavePrueba").orEmpty()
        val nombre = argumentos.getString("oylNombrePrueba").orEmpty()
        assumeTrue(usuario.isNotBlank() && clave.isNotBlank() && nombre.isNotBlank())

        if (desdeBienvenida) compose.onNodeWithText("Comenzar").performClick()
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
