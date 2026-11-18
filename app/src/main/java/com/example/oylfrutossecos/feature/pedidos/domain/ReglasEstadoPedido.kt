
package com.example.oylfrutossecos.feature.pedidos.domain

object ReglasEstadoPedido {

    // Estados a los que puede pasar un pedido.
    fun estadosSiguientes(
        estadoActual: String,
        modalidad: String
    ): List<String> {

        return when (estadoActual) {

            "Pendiente" ->
                listOf("En preparación", "Cancelado")

            "En preparación" ->
                if (modalidad == "Retiro") {
                    listOf("Listo para retiro", "Cancelado")
                } else {
                    listOf("En despacho", "Cancelado")
                }

            "Listo para retiro", "En despacho" ->
                listOf("Entregado")

            else -> emptyList()
        }
    }

    // Verifica si una transición está permitida.
    fun puedeCambiarEstado(
        estadoActual: String,
        nuevoEstado: String,
        modalidad: String
    ): Boolean {

        return nuevoEstado in estadosSiguientes(
            estadoActual,
            modalidad
        )
    }
}
