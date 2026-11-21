package com.example.oylfrutossecos

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
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
    fun administraProductosInventarioYPedidos() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("oylProbarAdministracion") == "true")
        iniciarSesion()
        esperarTexto("Resumen de la tienda")
        val producto = "Almendras naturales"
        fun buscarProducto(nombre: String) {
            esperarTexto("Buscar producto")
            compose.onNode(hasSetTextAction() and hasText("Buscar producto")).performTextInput(nombre)
        }

        compose.onNode(hasClickAction() and hasText("Productos")).performScrollTo().performClick()
        buscarProducto(producto)
        compose.onNodeWithText("Editar").performClick()
        compose.onNode(hasSetTextAction() and hasText("Nombre")).performTextReplacement("Almendras USB")
        compose.onNodeWithText("Guardar cambios").performScrollTo().performClick()
        esperarTexto("Gestión de Productos")
        compose.onNodeWithText("← Volver al inicio").performClick()

        compose.onNode(hasClickAction() and hasText("Inventario")).performScrollTo().performClick()
        buscarProducto("Almendras USB")
        compose.onNodeWithText("Ajustar stock").performClick()
        compose.onNode(hasSetTextAction() and hasText("Cantidad")).performTextInput("2")
        compose.onNodeWithText("Guardar ajuste").performClick()
        esperarTexto("Entrada de 2 unidades registrada correctamente para Almendras USB.")
        compose.onNodeWithText("← Volver al inicio").performClick()

        compose.onNodeWithText("Pedidos").performClick()
        esperarTexto("Ver detalles")
        compose.onNodeWithText("Ver detalles").performClick()
        compose.onNodeWithText("Cambiar a: En preparación").performScrollTo().performClick()
        compose.onNodeWithText("Confirmar").performClick()
        esperarTexto("Estado: En preparación")
    }

    @Test
    fun guardaElPerfilYLoRecuperaAlVolverAEntrar() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("oylProbarPerfil") == "true")
        iniciarSesion()
        fun abrirPerfil() {
            compose.onNodeWithContentDescription("Perfil").performClick()
            esperarTexto("Editar perfil")
        }

        abrirPerfil()
        compose.onNodeWithText("Editar perfil").performScrollTo().performClick()
        val direccion = "Av. Prueba USB 123"
        compose.onNode(hasSetTextAction() and hasText("Dirección (opcional)"))
            .performScrollTo().performTextReplacement(direccion)
        compose.onNodeWithText("Guardar cambios").performScrollTo().performClick()
        esperarTexto("Perfil actualizado.")
        compose.onNodeWithText(direccion).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Cerrar sesión").performScrollTo().performClick()

        iniciarSesion(desdeBienvenida = false)
        abrirPerfil()
        compose.onNodeWithText(direccion).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun registraUnaCuentaEIniciaSesionDesdeAndroid() {
        val argumentos = InstrumentationRegistry.getArguments()
        val clave = argumentos.getString("oylClavePrueba").orEmpty()
        assumeTrue(argumentos.getString("oylProbarRegistro") == "true" && clave.isNotBlank())
        val usuario = argumentos.getString("oylUsuarioRegistro")
            ?: ("nuevo_" + java.util.UUID.randomUUID().toString().take(8))
        val nombre = argumentos.getString("oylNombrePrueba") ?: "Cliente Nuevo USB"

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
        esperarTexto("¡Hola, ${nombre.substringBefore(' ')}!")
        compose.onNodeWithContentDescription("Pedidos").performClick()
        esperarTexto("Aún no tienes pedidos registrados.")
    }

    @Test
    fun clienteVeElEstadoActualizadoPorElAdministrador() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("oylProbarEstadoFinal") == "true")
        iniciarSesion()
        compose.onNodeWithContentDescription("Pedidos").performClick()
        esperarTexto("En preparación")
        compose.onNodeWithText("En preparación").assertIsDisplayed()
    }

    @Test
    fun iniciaYTerminaSesionConUnaCuentaDelBackend() {
        iniciarSesion()
        compose.onNodeWithContentDescription("Perfil").performClick()
        esperarTexto("Mi perfil")
        compose.onNodeWithText("Cerrar sesión").performScrollTo().performClick()
        compose.onNodeWithText("Iniciar sesión").assertIsDisplayed()
    }

    @Test
    fun compraDesdeElCarritoYConsultaElHistorial() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("oylProbarCompra") == "true")
        iniciarSesion()
        compose.onNode(hasClickAction() and hasText("Catálogo")).performScrollTo().performClick()
        esperarTexto("Almendras naturales")
        compose.onNodeWithText("Almendras naturales").performClick()
        compose.onNodeWithText("Agregar al carrito").performScrollTo().performClick()
        compose.onNode(hasClickAction() and hasText("Mi carrito") and hasText("1")).performClick()
        esperarTexto("Continuar al pedido")
        compose.onNodeWithText("Continuar al pedido").performClick()
        compose.onNode(hasSetTextAction() and hasText("Teléfono de contacto"))
            .performScrollTo().performTextInput("+56912345678")
        compose.onNode(hasClickAction() and hasText("Confirmar pedido"))
            .performScrollTo().performClick()
        esperarTexto("¡Pedido confirmado!")
        compose.onNodeWithText("Total: ${precioOYL(4500)}").assertIsDisplayed()
        compose.onNodeWithText("Ver mis pedidos").performClick()
        esperarTexto("Mis Pedidos")
        esperarTexto("Pendiente")
        compose.onNodeWithText("Pendiente").assertIsDisplayed()
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
        val saludo = if (argumentos.getString("oylProbarAdministracion") == "true") {
            "Hola, administrador"
        } else {
            "¡Hola, ${nombre.trim().substringBefore(' ')}!"
        }
        esperarTexto(saludo)
        compose.onNodeWithText(saludo).assertIsDisplayed()
    }

    private fun esperarTexto(texto: String) {
        compose.waitUntil(25_000) {
            compose.onAllNodesWithText(texto).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
