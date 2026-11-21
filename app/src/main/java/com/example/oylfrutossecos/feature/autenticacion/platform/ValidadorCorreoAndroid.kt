
package com.example.oylfrutossecos.feature.autenticacion.platform

import android.util.Patterns

object ValidadorCorreoAndroid {

    fun esValido(correo: String): Boolean {
        return Patterns.EMAIL_ADDRESS
            .matcher(correo)
            .matches()
    }
}
