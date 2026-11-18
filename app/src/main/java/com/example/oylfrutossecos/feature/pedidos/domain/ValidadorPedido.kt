
package com.example.oylfrutossecos.feature.pedidos.domain

object ValidadorPedido {

    fun esNombreValido(nombre: String): Boolean {
        return nombre.trim().length >= 3
    }

    fun esTelefonoValido(telefono: String): Boolean {
        return telefono.count { it.isDigit() } >= 9
    }

    fun esDireccionValida(
        modalidad: String,
        direccion: String
    ): Boolean {
        return modalidad == "Retiro" ||
                direccion.trim().length >= 10
    }

    fun puedeConfirmar(
        nombre: String,
        telefono: String,
        modalidad: String,
        direccion: String,
        tieneProductos: Boolean
    ): Boolean {
        return esNombreValido(nombre) &&
                esTelefonoValido(telefono) &&
                esDireccionValida(modalidad, direccion) &&
                tieneProductos
    }
}
