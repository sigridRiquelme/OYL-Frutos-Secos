
package com.example.oylfrutossecos.feature.carrito.domain

import com.example.oylfrutossecos.data.model.ProductoCatalogo

object CalculadoraCarrito {

    // Calcula el total usando los precios actuales.
    fun calcularTotal(
        productos: List<ProductoCatalogo>,
        actualesPorId: Map<Int, ProductoCatalogo>,
        cantidades: Map<Int, Int>
    ): Long {
        return productos.sumOf { producto ->

            val precioActual =
                actualesPorId[producto.id]?.precio
                    ?: producto.precio

            val cantidad =
                cantidades[producto.id] ?: 0

            precioActual.toLong() * cantidad
        }
    }

    // Detecta cambios que el cliente debe revisar.
    fun requiereRevision(
        productos: List<ProductoCatalogo>,
        actualesPorId: Map<Int, ProductoCatalogo>,
        cantidades: Map<Int, Int>
    ): Boolean {
        return productos.any { producto ->

            val actual = actualesPorId[producto.id]
            val cantidad = cantidades[producto.id] ?: 0

            actual == null ||
                    !actual.activo ||
                    cantidad > actual.stock ||
                    actual.precio != producto.precio
        }
    }
}
