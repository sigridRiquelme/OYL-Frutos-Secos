
package com.example.oylfrutossecos.feature.inventario.domain

object ReglasAjusteInventario {

    // Validar la cantidad ingresada por el administrador.
    // null significa que la cantidad es válida.
    fun validarCantidad(cantidadTexto: String): String? {

        val cantidad = cantidadTexto.trim().toIntOrNull()

        if (cantidad == null) {
            return "Ingresa una cantidad entera válida"
        }

        if (cantidad <= 0) {
            return "La cantidad debe ser mayor que cero"
        }

        return null
    }

    // Convertir una entrada o salida en su variación.
    // Entrada: cantidad positiva.
    // Salida: cantidad negativa.
    fun calcularVariacion(
        cantidad: Int,
        esEntrada: Boolean
    ): Int {

        require(cantidad > 0) {
            "La cantidad debe ser mayor que cero"
        }

        return if (esEntrada) {
            cantidad
        } else {
            -cantidad
        }
    }
}
