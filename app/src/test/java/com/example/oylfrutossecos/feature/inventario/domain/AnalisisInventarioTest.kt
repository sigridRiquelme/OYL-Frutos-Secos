
package com.example.oylfrutossecos.feature.inventario.domain

import com.example.oylfrutossecos.data.model.ProductoCatalogo
import io.kotest.matchers.shouldBe
import org.junit.Test

class AnalisisInventarioTest {

    private fun producto(
        id: Int,
        nombre: String,
        stock: Int
    ) = ProductoCatalogo(
        id = id,
        nombre = nombre,
        categoria = "Frutos secos",
        precio = 3000,
        stock = stock
    )

    private val productos = listOf(
        producto(1, "Almendras", 20),
        producto(2, "Nueces", 10),
        producto(3, "Pasas", 1),
        producto(4, "Mani", 0),
        producto(5, "Avellanas", 4)
    )

    // El producto 5 está desactivado
    private val estados = mapOf(
        1 to true,
        2 to true,
        3 to true,
        4 to true,
        5 to false
    )

    @Test
    fun resumenInventarioVacio() {
        val resumen = AnalisisInventario.calcularResumen(
            emptyList(),
            emptyMap()
        )

        resumen.productosRegistrados shouldBe 0
        resumen.unidadesTotales shouldBe 0L
        resumen.productosStockBajo shouldBe 0
        resumen.productosAgotados shouldBe 0
    }

    @Test
    fun resumenInventarioCorrecto() {
        val resumen = AnalisisInventario.calcularResumen(
            productos,
            estados
        )

        resumen.productosRegistrados shouldBe 5
        resumen.unidadesTotales shouldBe 35L
        resumen.productosStockBajo shouldBe 2
        resumen.productosAgotados shouldBe 1
    }

    @Test
    fun stockDiezEsStockBajo() {
        val lista = listOf(
            producto(1, "Nueces", 10)
        )

        AnalisisInventario.calcularResumen(
            lista,
            emptyMap()
        ).productosStockBajo shouldBe 1
    }

    @Test
    fun stockOnceNoEsStockBajo() {
        val lista = listOf(
            producto(1, "Nueces", 11)
        )

        AnalisisInventario.calcularResumen(
            lista,
            emptyMap()
        ).productosStockBajo shouldBe 0
    }

    @Test
    fun filtroTodosIncluyeInactivos() {
        val resultado = AnalisisInventario.filtrarProductos(
            productos,
            estados,
            busqueda = "",
            filtro = "Todos"
        )

        resultado.size shouldBe 5
    }

    @Test
    fun filtroPorReponer() {
        val resultado = AnalisisInventario.filtrarProductos(
            productos,
            estados,
            busqueda = "",
            filtro = "Por reponer"
        )

        resultado.map { it.id } shouldBe listOf(2, 3, 4)
    }

    @Test
    fun filtroStockBajo() {
        val resultado = AnalisisInventario.filtrarProductos(
            productos,
            estados,
            busqueda = "",
            filtro = "Stock bajo"
        )

        resultado.map { it.id } shouldBe listOf(2, 3)
    }

    @Test
    fun filtroAgotados() {
        val resultado = AnalisisInventario.filtrarProductos(
            productos,
            estados,
            busqueda = "",
            filtro = "Agotados"
        )

        resultado.map { it.id } shouldBe listOf(4)
    }

    @Test
    fun buscarSinDistinguirMayusculas() {
        val resultado = AnalisisInventario.filtrarProductos(
            productos,
            estados,
            busqueda = "ALM",
            filtro = "Todos"
        )

        resultado.map { it.id } shouldBe listOf(1)
    }

    @Test
    fun buscarDentroDeUnFiltro() {
        val resultado = AnalisisInventario.filtrarProductos(
            productos,
            estados,
            busqueda = "nue",
            filtro = "Stock bajo"
        )

        resultado.map { it.id } shouldBe listOf(2)
    }
}
