
package com.example.oylfrutossecos.feature.carrito.domain

import com.example.oylfrutossecos.data.model.ProductoCatalogo

object ReglasCarrito {

    // Comprueba si se pueden agregar unidades.
    fun puedeAgregar(
        producto: ProductoCatalogo,
        cantidadSolicitada: Int,
        cantidadActual: Int
    ): Boolean {

        val disponible =
            (producto.stock - cantidadActual).coerceAtLeast(0)

        return producto.activo &&
                cantidadSolicitada in 1..disponible
    }

    // Comprueba si puede modificarse la cantidad.
    fun puedeCambiarCantidad(
        producto: ProductoCatalogo?,
        cantidadAnterior: Int,
        nuevaCantidad: Int
    ): Boolean {

        if (producto == null || !producto.activo) {
            return false
        }

        if (nuevaCantidad <= 0) {
            return false
        }

        // Permitir reducir cantidades incluso cuando
        // el stock disminuyó desde que se agregó.
        return nuevaCantidad < cantidadAnterior ||
                nuevaCantidad <= producto.stock
    }
}
