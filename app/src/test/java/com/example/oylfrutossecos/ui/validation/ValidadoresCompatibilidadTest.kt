package com.example.oylfrutossecos.ui.validation

import org.junit.Test
import org.junit.Assert.*

class ValidadoresCompatibilidadTest {
    @Test fun inventarioConservaValidacionCentralizada() {
        assertNull(ValidadorInventario.validarCantidad("2"))
        for (valor in listOf("", "0", "-1", "abc", "99999999999999999")) assertNotNull(ValidadorInventario.validarCantidad(valor))
    }
}
