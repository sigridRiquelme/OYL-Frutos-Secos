
package com.example.oylfrutossecos.feature.pedidos.domain

import com.example.oylfrutossecos.data.model.DetallePedido
import com.example.oylfrutossecos.data.model.ProductoCatalogo

import io.kotest.matchers.shouldBe
import org.junit.Test

class PreparadorDetallesPedidoTest {

    private val almendras = ProductoCatalogo(
        id = 1,
        nombre = "Almendras",
        categoria = "Frutos secos",
        precio = 3000,
        stock = 20
    )

    private val nueces = ProductoCatalogo(
        id = 2,
        nombre = "Nueces",
        categoria = "Frutos secos",
        precio = 4500,
        stock = 15
    )

    private val mani = ProductoCatalogo(
        id = 3,
        nombre = "Mani",
        categoria = "Frutos secos",
        precio = 2000,
        stock = 30
    )

    // PRUEBA 1
    @Test
    fun preparaUnProductoCorrectamente() {

        val resultado = PreparadorDetallesPedido.desdeCarrito(
            productos = listOf(almendras),
            cantidades = mapOf(1 to 2)
        )

        resultado shouldBe listOf(
            DetallePedido(
                idProducto = 1,
                nombre = "Almendras",
                cantidad = 2,
                precioUnitario = 3000
            )
        )
    }

    // PRUEBA 2
    @Test
    fun preparaVariosProductos() {

        val resultado = PreparadorDetallesPedido.desdeCarrito(
            productos = listOf(almendras, nueces),
            cantidades = mapOf(
                1 to 2,
                2 to 3
            )
        )

        resultado.size shouldBe 2
        resultado[0].cantidad shouldBe 2
        resultado[1].cantidad shouldBe 3
    }

    // PRUEBA 3
    @Test
    fun omiteProductosConCantidadCero() {

        val resultado = PreparadorDetallesPedido.desdeCarrito(
            productos = listOf(almendras, nueces),
            cantidades = mapOf(
                1 to 2,
                2 to 0
            )
        )

        resultado.size shouldBe 1
        resultado[0].idProducto shouldBe 1
    }

    // PRUEBA 4
    @Test
    fun omiteProductosSinCantidadRegistrada() {

        val resultado = PreparadorDetallesPedido.desdeCarrito(
            productos = listOf(almendras, nueces),
            cantidades = mapOf(1 to 1)
        )

        resultado.size shouldBe 1
        resultado[0].nombre shouldBe "Almendras"
    }

    // PRUEBA 5
    @Test
    fun omiteCantidadesNegativas() {

        val resultado = PreparadorDetallesPedido.desdeCarrito(
            productos = listOf(almendras),
            cantidades = mapOf(1 to -2)
        )

        resultado shouldBe emptyList()
    }

    // PRUEBA 6
    @Test
    fun carritoVacioProduceListaVacia() {

        val resultado = PreparadorDetallesPedido.desdeCarrito(
            productos = emptyList(),
            cantidades = emptyMap()
        )

        resultado shouldBe emptyList()
    }

    // PRUEBA 7
    @Test
    fun conservaLosPreciosOriginales() {

        val resultado = PreparadorDetallesPedido.desdeCarrito(
            productos = listOf(almendras, nueces),
            cantidades = mapOf(
                1 to 5,
                2 to 2
            )
        )

        resultado[0].precioUnitario shouldBe 3000
        resultado[1].precioUnitario shouldBe 4500
    }

    // PRUEBA 8
    @Test
    fun conservaElOrdenDeLosProductos() {

        val resultado = PreparadorDetallesPedido.desdeCarrito(
            productos = listOf(mani, almendras, nueces),
            cantidades = mapOf(
                1 to 1,
                2 to 1,
                3 to 1
            )
        )

        resultado.map { it.idProducto } shouldBe listOf(3, 1, 2)
    }
}
