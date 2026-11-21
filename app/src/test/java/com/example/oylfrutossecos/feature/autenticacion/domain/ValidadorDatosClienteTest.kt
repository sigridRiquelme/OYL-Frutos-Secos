package com.example.oylfrutossecos.feature.autenticacion.domain

import org.junit.Test
import org.junit.Assert.*

class ValidadorDatosClienteTest {
    @Test fun verificaLimitesYCaracteresDeDatosPersonales() {
        assertFalse(ValidadorDatosCliente.nombreValido("ab"))
        assertTrue(ValidadorDatosCliente.nombreValido(" Ana "))
        assertTrue(ValidadorDatosCliente.nombreValido("a".repeat(120)))
        assertFalse(ValidadorDatosCliente.nombreValido("a".repeat(121)))
        for (usuario in listOf("abc", "cliente.1_-", "a".repeat(80))) assertTrue(ValidadorDatosCliente.usuarioValido(usuario))
        for (usuario in listOf("ab", "a".repeat(81), "con espacio", "<script>")) assertFalse(ValidadorDatosCliente.usuarioValido(usuario))
        assertTrue(ValidadorDatosCliente.telefonoValido("+56 (9) 1234-5678"))
        assertFalse(ValidadorDatosCliente.telefonoValido("12345678"))
        assertFalse(ValidadorDatosCliente.telefonoValido("912345678x"))
        assertFalse(ValidadorDatosCliente.telefonoValido("1".repeat(31)))
        assertTrue(ValidadorDatosCliente.direccionValida(""))
        assertTrue(ValidadorDatosCliente.direccionValida("a".repeat(250)))
        assertFalse(ValidadorDatosCliente.direccionValida("a".repeat(251)))
    }
}
