
package com.example.oylfrutossecos.feature.autenticacion.domain

object ValidadorRegistro {

    // Nombre: mínimo 3 caracteres
    fun esNombreValido(nombre: String): Boolean {
        return nombre.trim().length >= 3
    }

    // Nombre de usuario: mínimo 3 caracteres
    fun esUsuarioValido(usuario: String): Boolean {
        return usuario.trim().length >= 3
    }

    // Teléfono: mínimo 9 dígitos
    fun esTelefonoValido(telefono: String): Boolean {
        return telefono.count { it.isDigit() } >= 9
    }

    // Contraseña: mínimo 6 caracteres
    fun esContrasenaValida(contrasena: String): Boolean {
        return contrasena.length >= 6
    }

    // Comprobar todos los datos del registro
    fun esRegistroValido(
        nombre: String,
        usuario: String,
        correoValido: Boolean,
        telefono: String,
        contrasena: String
    ): Boolean {
        return esNombreValido(nombre) &&
                esUsuarioValido(usuario) &&
                correoValido &&
                esTelefonoValido(telefono) &&
                esContrasenaValida(contrasena)
    }
}
