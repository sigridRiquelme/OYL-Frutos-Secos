
package com.example.oylfrutossecos.feature.autenticacion.domain

import io.kotest.matchers.shouldBe
import org.junit.Test

class SeguridadContrasenaTest {

    // PRUEBA 1: Contraseña correcta
    @Test
    fun contrasenaCorrecta() {

        val credenciales =
            SeguridadContrasena.generarHash(
                "Prueba123!"
            )

        SeguridadContrasena.verificarContrasena(
            contrasena = "Prueba123!",
            hashGuardado = credenciales.hash,
            saltGuardado = credenciales.salt
        ) shouldBe true
    }

    // PRUEBA 2: Contraseña incorrecta
    @Test
    fun contrasenaIncorrecta() {

        val credenciales =
            SeguridadContrasena.generarHash(
                "Prueba123!"
            )

        SeguridadContrasena.verificarContrasena(
            contrasena = "OtraClave123!",
            hashGuardado = credenciales.hash,
            saltGuardado = credenciales.salt
        ) shouldBe false
    }

    // PRUEBA 3: Dos registros generan salts distintos
    @Test
    fun generarSaltAleatorio() {

        val primera =
            SeguridadContrasena.generarHash(
                "Prueba123!"
            )

        val segunda =
            SeguridadContrasena.generarHash(
                "Prueba123!"
            )

        (primera.salt != segunda.salt) shouldBe true
        (primera.hash != segunda.hash) shouldBe true
    }

    // PRUEBA 4: Nunca guardar texto plano
    @Test
    fun contrasenaNoSeGuardaEnTextoPlano() {

        val credenciales =
            SeguridadContrasena.generarHash(
                "Prueba123!"
            )

        (credenciales.hash != "Prueba123!") shouldBe true
        (credenciales.salt.isNotBlank()) shouldBe true
    }
}
