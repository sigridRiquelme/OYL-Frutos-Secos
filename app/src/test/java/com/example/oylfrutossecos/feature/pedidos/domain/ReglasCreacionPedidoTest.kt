
package com.example.oylfrutossecos.feature.pedidos.domain

import com.example.oylfrutossecos.data.model.DetallePedido

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe

import org.junit.Test

class ReglasCreacionPedidoTest {

    private fun producto(
        id: Int = 1,
        cantidad: Int = 2,
        precio: Int = 3000
    ) = DetallePedido(
        idProducto = id,
        nombre = "Almendras",
        cantidad = cantidad,
        precioUnitario = precio
    )

    // PRUEBA 1: Total de un producto
    @Test
    fun totalDeUnProducto() {

        val productos = listOf(
            producto(cantidad = 2, precio = 3000)
        )

        ReglasCreacionPedido.calcularTotal(
            productos
        ) shouldBe 6000
    }

    // PRUEBA 2: Total de varios productos
    @Test
    fun totalDeVariosProductos() {

        val productos = listOf(
            producto(id = 1, cantidad = 2, precio = 3000),
            producto(id = 2, cantidad = 3, precio = 2000)
        )

        ReglasCreacionPedido.calcularTotal(
            productos
        ) shouldBe 12000
    }

    // PRUEBA 3: No permitir pedidos vacíos
    @Test
    fun noPermitirPedidoVacio() {

        shouldThrow<IllegalArgumentException> {
            ReglasCreacionPedido.calcularTotal(
                emptyList()
            )
        }
    }

    // PRUEBA 4: Cantidad cero
    @Test
    fun noPermitirCantidadCero() {

        shouldThrow<IllegalArgumentException> {
            ReglasCreacionPedido.calcularTotal(
                listOf(producto(cantidad = 0))
            )
        }
    }

    // PRUEBA 5: Cantidad negativa
    @Test
    fun noPermitirCantidadNegativa() {

        shouldThrow<IllegalArgumentException> {
            ReglasCreacionPedido.calcularTotal(
                listOf(producto(cantidad = -3))
            )
        }
    }

    // PRUEBA 6: Precio negativo
    @Test
    fun noPermitirPrecioNegativo() {

        shouldThrow<IllegalArgumentException> {
            ReglasCreacionPedido.calcularTotal(
                listOf(producto(precio = -100))
            )
        }
    }

    // PRUEBA 7: Precio cero permitido
    @Test
    fun permitirPrecioCero() {

        ReglasCreacionPedido.calcularTotal(
            listOf(producto(precio = 0))
        ) shouldBe 0
    }

    // PRUEBA 8: Evitar total fuera de rango
    @Test
    fun rechazarTotalDemasiadoGrande() {

        shouldThrow<IllegalArgumentException> {
            ReglasCreacionPedido.calcularTotal(
                listOf(
                    producto(
                        cantidad = 3,
                        precio = Int.MAX_VALUE
                    )
                )
            )
        }
    }

    // PRUEBA 9: Máximo total permitido
    @Test
    fun aceptarTotalMaximo() {

        ReglasCreacionPedido.calcularTotal(
            listOf(
                producto(
                    cantidad = 1,
                    precio = Int.MAX_VALUE
                )
            )
        ) shouldBe Int.MAX_VALUE
    }

    // PRUEBA 10: La suma también debe respetar el límite
    @Test
    fun rechazarSumaDeProductosExcesiva() {

        shouldThrow<IllegalArgumentException> {
            ReglasCreacionPedido.calcularTotal(
                listOf(
                    producto(
                        id = 1,
                        cantidad = 1,
                        precio = Int.MAX_VALUE
                    ),
                    producto(
                        id = 2,
                        cantidad = 1,
                        precio = 1
                    )
                )
            )
        }
    }
}
