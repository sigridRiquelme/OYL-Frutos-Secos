
package com.example.oylfrutossecos.feature.autenticacion.domain

import io.kotest.matchers.shouldBe
import org.junit.Test

class ValidadorRegistroTest {

    // PRUEBA 1: Nombre válido
    @Test
    fun nombreCorrecto() {
        ValidadorRegistro.esNombreValido(
            "Cliente Prueba"
        ) shouldBe true
    }

    // PRUEBA 2: Nombre demasiado corto
    @Test
    fun nombreCorto() {
        ValidadorRegistro.esNombreValido(
            "Ab"
        ) shouldBe false
    }

    // PRUEBA 3: Usuario válido
    @Test
    fun usuarioCorrecto() {
        ValidadorRegistro.esUsuarioValido(
            "cliente123"
        ) shouldBe true
    }

    // PRUEBA 4: Usuario demasiado corto
    @Test
    fun usuarioInvalido() {
        ValidadorRegistro.esUsuarioValido(
            "ab"
        ) shouldBe false
    }

    // PRUEBA 5: Teléfono correcto
    @Test
    fun telefonoCorrecto() {
        ValidadorRegistro.esTelefonoValido(
            "+56 9 1234 5678"
        ) shouldBe true
    }

    // PRUEBA 6: Teléfono inválido
    @Test
    fun telefonoCorto() {
        ValidadorRegistro.esTelefonoValido(
            "12345"
        ) shouldBe false
    }

    // PRUEBA 7: Contraseña válida
    @Test
    fun contrasenaCorrecta() {
        ValidadorRegistro.esContrasenaValida(
            "Prueba123!"
        ) shouldBe true
    }

    // PRUEBA 8: Contraseña demasiado corta
    @Test
    fun contrasenaCorta() {
        ValidadorRegistro.esContrasenaValida(
            "123"
        ) shouldBe false
    }

    // PRUEBA 9: Registro completo válido
    @Test
    fun registroCorrecto() {
        ValidadorRegistro.esRegistroValido(
            nombre = "Cliente Prueba",
            usuario = "cliente123",
            correoValido = true,
            telefono = "912345678",
            contrasena = "Prueba123!"
        ) shouldBe true
    }

    // PRUEBA 10: Correo rechazado
    @Test
    fun registroConCorreoInvalido() {
        ValidadorRegistro.esRegistroValido(
            nombre = "Cliente Prueba",
            usuario = "cliente123",
            correoValido = false,
            telefono = "912345678",
            contrasena = "Prueba123!"
        ) shouldBe false
    }

    // PRUEBA 11: Registro con teléfono inválido
    @Test
    fun registroConTelefonoInvalido() {
        ValidadorRegistro.esRegistroValido(
            nombre = "Cliente Prueba",
            usuario = "cliente123",
            correoValido = true,
            telefono = "123",
            contrasena = "Prueba123!"
        ) shouldBe false
    }

    // PRUEBA 12: Registro con nombre inválido
    @Test
    fun registroConNombreInvalido() {
        ValidadorRegistro.esRegistroValido(
            nombre = "Ab",
            usuario = "cliente123",
            correoValido = true,
            telefono = "912345678",
            contrasena = "Prueba123!"
        ) shouldBe false
    }
}
