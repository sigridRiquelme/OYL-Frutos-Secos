
package com.example.oylfrutossecos.feature.pedidos.domain

import com.example.oylfrutossecos.data.model.DetallePedido

object ReglasCreacionPedido {

    // Validar los productos de una compra
    fun validarProductos(
        productos: List<DetallePedido>
    ) {

        require(productos.isNotEmpty()) {
            "El pedido debe contener productos."
        }

        require(productos.all {
            it.cantidad > 0 && it.precioUnitario >= 0
        }) {
            "Las cantidades y los precios deben ser válidos."
        }
    }

    // Calcular el total del pedido de forma segura
    fun calcularTotal(
        productos: List<DetallePedido>
    ): Int {

        validarProductos(productos)

        var total = 0L

        productos.forEach { producto ->

            val subtotal =
                producto.precioUnitario.toLong() *
                        producto.cantidad.toLong()

            // Evitar desbordamientos monetarios
            require(
                subtotal <= Int.MAX_VALUE.toLong() - total
            ) {
                "El total del pedido supera el monto permitido."
            }

            total += subtotal
        }

        return total.toInt()
    }
}
