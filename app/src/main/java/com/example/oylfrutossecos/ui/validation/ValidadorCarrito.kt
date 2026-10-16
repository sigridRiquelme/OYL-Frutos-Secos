
package com.example.oylfrutossecos.ui.validation

import com.example.oylfrutossecos.data.local.entity.ProductoEntity

object ValidadorCarrito {

    // null = carrito válido
    // String = explicación del problema encontrado
    fun validar(
        cantidades: Map<Int, Int>,
        preciosCarrito: Map<Int, Int>,
        productosActuales: List<ProductoEntity>
    ): String? {

        // 1. No permitir pedidos vacíos
        if (cantidades.isEmpty()) {
            return "El carrito está vacío."
        }

        // Buscar productos actuales por identificador
        val productosPorId = productosActuales.associateBy {
            it.id
        }

        var total = 0L

        // 2. Revisar todos los productos del carrito
        for ((id, cantidad) in cantidades) {

            if (cantidad <= 0) {
                return "Hay una cantidad inválida en el carrito."
            }

            val producto = productosPorId[id]
                ?: return "Un producto del carrito ya no existe."

            // 3. Comprobar que esté activo
            if (!producto.activo) {
                return "El producto ${producto.nombre} " +
                        "ya no está disponible para la venta."
            }

            // 4. Comprobar stock actualizado
            if (cantidad > producto.stock) {
                return "Stock insuficiente para ${producto.nombre}. " +
                        "Solicitaste $cantidad unidades, pero " +
                        "solo hay ${producto.stock} disponibles."
            }

            // 5. Comparar el precio original con el actual
            val precioAnterior = preciosCarrito[id]
                ?: return "No se pudo verificar el precio " +
                        "de ${producto.nombre}."

            if (producto.precio != precioAnterior) {
                return "El precio de ${producto.nombre} cambió. " +
                        "Revisa el total actualizado antes de continuar."
            }

            // 6. Evitar cantidades monetarias fuera del rango
            if (producto.precio < 0) {
                return "El producto ${producto.nombre} " +
                        "tiene un precio inválido."
            }

            total += producto.precio.toLong() * cantidad

            if (total > Int.MAX_VALUE) {
                return "El total del pedido supera " +
                        "el monto permitido."
            }
        }

        // Todos los productos pasaron las validaciones
        return null
    }
}

