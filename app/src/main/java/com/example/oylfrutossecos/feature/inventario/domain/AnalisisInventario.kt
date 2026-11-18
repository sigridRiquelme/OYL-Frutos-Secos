
package com.example.oylfrutossecos.feature.inventario.domain

import com.example.oylfrutossecos.data.model.ProductoCatalogo

data class ResumenInventario(
    val productosRegistrados: Int,
    val unidadesTotales: Long,
    val productosStockBajo: Int,
    val productosAgotados: Int
)

object AnalisisInventario {

    const val LIMITE_STOCK_BAJO = 10

    // Obtener los indicadores del inventario
    fun calcularResumen(
        productos: List<ProductoCatalogo>,
        estadosProductos: Map<Int, Boolean>
    ): ResumenInventario {

        val stockBajo = productos.count { producto ->
            val activo = estadosProductos[producto.id] ?: true

            activo && producto.stock in 1..LIMITE_STOCK_BAJO
        }

        val agotados = productos.count { producto ->
            val activo = estadosProductos[producto.id] ?: true

            activo && producto.stock == 0
        }

        val unidades = productos.sumOf {
            it.stock.toLong()
        }

        return ResumenInventario(
            productosRegistrados = productos.size,
            unidadesTotales = unidades,
            productosStockBajo = stockBajo,
            productosAgotados = agotados
        )
    }

    // Buscar y filtrar productos
    fun filtrarProductos(
        productos: List<ProductoCatalogo>,
        estadosProductos: Map<Int, Boolean>,
        busqueda: String,
        filtro: String
    ): List<ProductoCatalogo> {

        return productos.filter { producto ->

            val coincideNombre = producto.nombre.contains(
                busqueda,
                ignoreCase = true
            )

            val activo = estadosProductos[producto.id] ?: true

            val coincideFiltro = when (filtro) {

                "Por reponer" ->
                    activo &&
                            producto.stock in 0..LIMITE_STOCK_BAJO

                "Stock bajo" ->
                    activo &&
                            producto.stock in 1..LIMITE_STOCK_BAJO

                "Agotados" ->
                    activo && producto.stock == 0

                else -> true
            }

            coincideNombre && coincideFiltro
        }
    }
}
