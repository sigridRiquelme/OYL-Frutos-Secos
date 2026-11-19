
package com.example.oylfrutossecos.feature.pedidos.domain

import com.example.oylfrutossecos.data.model.DetallePedido
import com.example.oylfrutossecos.data.model.ProductoCatalogo

object PreparadorDetallesPedido {

    // Transforma los productos del carrito en
    // detalles listos para registrar un pedido.
    fun desdeCarrito(
        productos: Collection<ProductoCatalogo>,
        cantidades: Map<Int, Int>
    ): List<DetallePedido> {

        return productos.mapNotNull { producto ->

            val cantidad = cantidades[producto.id] ?: 0

            if (cantidad > 0) {
                DetallePedido(
                    idProducto = producto.id,
                    nombre = producto.nombre,
                    cantidad = cantidad,
                    precioUnitario = producto.precio
                )
            } else {
                null
            }
        }
    }
}
