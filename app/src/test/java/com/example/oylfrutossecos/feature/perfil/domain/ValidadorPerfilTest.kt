
package com.example.oylfrutossecos.feature.perfil.domain

import io.kotest.matchers.shouldBe
import org.junit.Test

class ValidadorPerfilTest {

    // PRUEBA 1: Perfil correcto
    @Test
    fun perfilCorrecto() {
        ValidadorPerfil.esPerfilValido(
            nombre = "Cliente Prueba",
            nombreUsuario = "cliente123",
            correoValido = true,
            telefono = "912345678"
        ) shouldBe true
    }

    // PRUEBA 2: Nombre demasiado corto
    @Test
    fun nombreInvalido() {
        ValidadorPerfil.esPerfilValido(
            nombre = "Ab",
            nombreUsuario = "cliente123",
            correoValido = true,
            telefono = "912345678"
        ) shouldBe false
    }

    // PRUEBA 3: Usuario demasiado corto
    @Test
    fun usuarioInvalido() {
        ValidadorPerfil.esPerfilValido(
            nombre = "Cliente Prueba",
            nombreUsuario = "ab",
            correoValido = true,
            telefono = "912345678"
        ) shouldBe false
    }

    // PRUEBA 4: Correo inválido
    @Test
    fun correoInvalido() {
        ValidadorPerfil.esPerfilValido(
            nombre = "Cliente Prueba",
            nombreUsuario = "cliente123",
            correoValido = false,
            telefono = "912345678"
        ) shouldBe false
    }

    // PRUEBA 5: Teléfono inválido
    @Test
    fun telefonoInvalido() {
        ValidadorPerfil.esPerfilValido(
            nombre = "Cliente Prueba",
            nombreUsuario = "cliente123",
            correoValido = true,
            telefono = "123"
        ) shouldBe false
    }

    // PRUEBA 6: Teléfono con separadores
    @Test
    fun telefonoConSeparadores() {
        ValidadorPerfil.esPerfilValido(
            nombre = "Cliente Prueba",
            nombreUsuario = "cliente123",
            correoValido = true,
            telefono = "+56 9 1234 5678"
        ) shouldBe true
    }
}
