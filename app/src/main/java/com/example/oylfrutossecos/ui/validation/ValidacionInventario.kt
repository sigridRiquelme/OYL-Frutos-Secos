
package com.example.oylfrutossecos.ui.validation

import com.example.oylfrutossecos.feature.inventario.domain.ReglasAjusteInventario

object ValidadorInventario {

    fun validarCantidad(cantidadTexto: String): String? {
        return ReglasAjusteInventario.validarCantidad(
            cantidadTexto
        )
    }
}
