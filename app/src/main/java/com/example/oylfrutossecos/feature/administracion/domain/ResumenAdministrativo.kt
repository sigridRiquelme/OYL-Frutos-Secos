
package com.example.oylfrutossecos.feature.administracion.domain

import com.example.oylfrutossecos.data.model.Pedido
import com.example.oylfrutossecos.data.model.ProductoCatalogo

// Información para las pantallas del administrador.
data class ResumenAdministrativo(
    val productosActivos: Int,
    val productosStockBajo: Int,
    val productosAgotados: Int,
    val alertasStock: Int,
    val pedidosPendientes: Int,
    val pedidosRegistrados: Int
) {
    val totalAvisos: Int
        get() = alertasStock + pedidosPendientes
}

object CalculadoraResumenAdministrativo {

    fun calcular(
        productos: List<ProductoCatalogo>,
        pedidos: List<Pedido>
    ): ResumenAdministrativo {

        // Considerar solamente productos activos.
        val productosActivos = productos.filter {
            it.activo
        }

        // Productos activos con stock entre 1 y 10.
        val stockBajo = productosActivos.count {
            it.stock in 1..10
        }

        // Productos activos sin unidades.
        val agotados = productosActivos.count {
            it.stock == 0
        }

        // Pedidos que requieren atención.
        val pendientes = pedidos.count {
            it.estado == "Pendiente"
        }

        return ResumenAdministrativo(
            productosActivos = productosActivos.size,
            productosStockBajo = stockBajo,
            productosAgotados = agotados,
            alertasStock = stockBajo + agotados,
            pedidosPendientes = pendientes,
            pedidosRegistrados = pedidos.size
        )
    }
}
