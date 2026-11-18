
package com.example.oylfrutossecos.feature.perfil.domain

import com.example.oylfrutossecos.feature.autenticacion.domain.ValidadorRegistro

object ValidadorPerfil {

    fun esPerfilValido(
        nombre: String,
        nombreUsuario: String,
        correoValido: Boolean,
        telefono: String
    ): Boolean {

        return ValidadorRegistro.esNombreValido(nombre) &&
                ValidadorRegistro.esUsuarioValido(nombreUsuario) &&
                correoValido &&
                ValidadorRegistro.esTelefonoValido(telefono)
    }
}
