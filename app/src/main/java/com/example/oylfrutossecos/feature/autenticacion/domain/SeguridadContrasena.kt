
package com.example.oylfrutossecos.feature.autenticacion.domain

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class CredencialesProtegidas(
    val hash: String,
    val salt: String
)

object SeguridadContrasena {

    private const val ITERACIONES = 210_000
    private const val LONGITUD_HASH = 256
    private const val LONGITUD_SALT = 16

    // Generar contraseña protegida para el registro
    fun generarHash(
        contrasena: String
    ): CredencialesProtegidas {

        val salt = ByteArray(LONGITUD_SALT)

        SecureRandom().nextBytes(salt)

        val hash = derivarHash(
            contrasena = contrasena,
            salt = salt,
            longitudBits = LONGITUD_HASH
        )

        return CredencialesProtegidas(
            hash = Base64.getEncoder()
                .encodeToString(hash),

            salt = Base64.getEncoder()
                .encodeToString(salt)
        )
    }

    // Verificar la contraseña durante el login
    fun verificarContrasena(
        contrasena: String,
        hashGuardado: String,
        saltGuardado: String
    ): Boolean {

        val salt = Base64.getDecoder()
            .decode(saltGuardado)

        val hashOriginal = Base64.getDecoder()
            .decode(hashGuardado)

        val hashIngresado = derivarHash(
            contrasena = contrasena,
            salt = salt,
            longitudBits = hashOriginal.size * 8
        )

        return MessageDigest.isEqual(
            hashOriginal,
            hashIngresado
        )
    }

    // Cálculo centralizado de PBKDF2
    private fun derivarHash(
        contrasena: String,
        salt: ByteArray,
        longitudBits: Int
    ): ByteArray {

        val spec = PBEKeySpec(
            contrasena.toCharArray(),
            salt,
            ITERACIONES,
            longitudBits
        )

        return try {
            SecretKeyFactory.getInstance(
                "PBKDF2WithHmacSHA256"
            ).generateSecret(spec).encoded

        } finally {
            spec.clearPassword()
        }
    }
}
