
package com.example.oylfrutossecos.feature.carrito.domain

import com.example.oylfrutossecos.data.model.ProductoCatalogo

// Resultado de revisar el carrito.
data class ResultadoRevisionCarrito(
    val error: String?,
    val productosActualizados: Map<Int, ProductoCatalogo>
)

object CoordinadorRevisionCarrito {

    fun revisar(
        cantidades: Map<Int, Int>,
        productosGuardados: Map<Int, ProductoCatalogo>,
        productosActuales: Collection<ProductoCatalogo>,
        validar: (
            Map<Int, Int>,
            Map<Int, Int>
        ) -> String?
    ): ResultadoRevisionCarrito {

        // Precios que tenía el carrito antes
        // de consultar los productos actuales.
        val preciosOriginales = productosGuardados.mapValues {
                (_, producto) -> producto.precio
        }

        // Utilizamos el validador existente.
        val error = validar(
            cantidades,
            preciosOriginales
        )

        // Si todo está correcto, no necesitamos
        // actualizar los productos del carrito.
        if (error == null) {
            return ResultadoRevisionCarrito(
                error = null,
                productosActualizados = emptyMap()
            )
        }

        // Recuperar los datos actuales solamente
        // de los productos presentes en el carrito.
        val productosParaActualizar = productosActuales
            .filter { producto ->
                producto.id in cantidades.keys
            }
            .associateBy { producto ->
                producto.id
            }

        return ResultadoRevisionCarrito(
            error = error,
            productosActualizados = productosParaActualizar
        )
    }
}
