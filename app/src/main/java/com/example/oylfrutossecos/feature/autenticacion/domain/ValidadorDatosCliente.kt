package com.example.oylfrutossecos.feature.autenticacion.domain

internal object ValidadorDatosCliente {
    private val usuario = Regex("[a-zA-Z0-9._-]{3,80}")
    private val telefono = Regex("[+0-9 ()-]{1,30}")

    fun nombreValido(valor: String): Boolean = valor.trim().length in 3..120

    fun usuarioValido(valor: String): Boolean = usuario.matches(valor.trim())

    fun telefonoValido(valor: String): Boolean =
        telefono.matches(valor.trim()) && valor.count { it in '0'..'9' } >= 9

    fun direccionValida(valor: String): Boolean = valor.trim().length <= 250
}
