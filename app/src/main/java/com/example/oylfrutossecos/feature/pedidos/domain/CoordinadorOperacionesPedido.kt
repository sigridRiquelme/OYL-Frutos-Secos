
package com.example.oylfrutossecos.feature.pedidos.domain

import com.example.oylfrutossecos.data.model.DetallePedido
import com.example.oylfrutossecos.data.model.Pedido
import com.example.oylfrutossecos.data.model.ProductoCatalogo

// Resultados posibles al confirmar un pedido
sealed interface ResultadoConfirmacionPedido {

    data class Registrado(
        val pedido: Pedido
    ) : ResultadoConfirmacionPedido

    data class CarritoInvalido(
        val mensaje: String
    ) : ResultadoConfirmacionPedido

    object ErrorGuardado : ResultadoConfirmacionPedido
}

object CoordinadorOperacionesPedido {

    // Validar el carrito, preparar los detalles
    // y ejecutar el registro del pedido.
    fun confirmar(
        validarCarrito: () -> String?,
        productos: Collection<ProductoCatalogo>,
        cantidades: Map<Int, Int>,
        registrar: (List<DetallePedido>) -> Pedido
    ): ResultadoConfirmacionPedido {

        val errorCarrito = validarCarrito()

        if (errorCarrito != null) {
            return ResultadoConfirmacionPedido.CarritoInvalido(
                errorCarrito
            )
        }

        return try {

            val detalles = PreparadorDetallesPedido.desdeCarrito(
                productos = productos,
                cantidades = cantidades
            )

            val pedido = registrar(detalles)

            ResultadoConfirmacionPedido.Registrado(pedido)

        } catch (error: Exception) {

            ResultadoConfirmacionPedido.ErrorGuardado
        }
    }

    // Ejecutar un cambio de estado.
    // Retorna null cuando funciona o un mensaje si falla.
    fun cambiarEstado(
        actualizar: () -> Unit
    ): String? {

        return try {

            actualizar()
            null

        } catch (error: Exception) {

            "No se pudo actualizar el estado del pedido."
        }
    }
}
