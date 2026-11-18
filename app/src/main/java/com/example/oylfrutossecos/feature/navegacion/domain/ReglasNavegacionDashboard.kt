
package com.example.oylfrutossecos.feature.navegacion.domain

object ReglasNavegacionDashboard {

    // Comprobar qué opción de la barra inferior
    // debe aparecer seleccionada.
    fun estaSeleccionada(
        opcion: String,
        pantallaActual: String
    ): Boolean {

        return opcion == pantallaActual ||
                (
                        opcion == "Inicio" &&
                                pantallaActual == "Catalogo"
                        )
    }

    // Mostrar el acceso rápido al carrito
    // únicamente cuando corresponda.
    fun mostrarAccesoCarrito(
        rol: String,
        pantallaActual: String
    ): Boolean {

        if (rol != "Cliente") {
            return false
        }

        return pantallaActual !in setOf(
            "Carrito",
            "Confirmacion",
            "PedidoExito"
        )
    }

    // Formatear la cantidad que aparece
    // en el acceso rápido al carrito.
    fun etiquetaCantidadCarrito(
        cantidad: Int
    ): String {

        return if (cantidad > 99) {
            "99+"
        } else {
            cantidad.toString()
        }
    }

}
