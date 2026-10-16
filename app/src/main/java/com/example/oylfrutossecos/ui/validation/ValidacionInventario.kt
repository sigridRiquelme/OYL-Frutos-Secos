
package com.example.oylfrutossecos.ui.validation

object ValidadorInventario {

    fun validarCantidad(cantidadTexto: String): String? {

        val cantidad = cantidadTexto.trim().toIntOrNull()

        if (cantidad == null) {
            return "Ingresa una cantidad entera válida"
        }

        if (cantidad <= 0) {
            return "La cantidad debe ser mayor que cero"
        }

        // null significa que no existen errores
        return null
    }
}
