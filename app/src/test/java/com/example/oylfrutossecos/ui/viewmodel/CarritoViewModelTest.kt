
package com.example.oylfrutossecos.ui.viewmodel

import com.example.oylfrutossecos.data.model.ProductoCatalogo
import io.kotest.matchers.shouldBe
import org.junit.Test

class CarritoViewModelTest {

    private val producto = ProductoCatalogo(
        id = 1,
        nombre = "Almendras",
        categoria = "Frutos secos",
        precio = 3000,
        stock = 10
    )

    @Test
    fun agregarActualizaCantidad() {
        val carrito = CarritoViewModel()

        carrito.agregar(producto, 2) shouldBe true
        carrito.cantidades[1] shouldBe 2
    }

    @Test
    fun agregarDosVecesAcumulaUnidades() {
        val carrito = CarritoViewModel()

        carrito.agregar(producto, 2)
        carrito.agregar(producto, 3)

        carrito.cantidades[1] shouldBe 5
    }

    @Test
    fun noAgregarMasQueElStock() {
        val carrito = CarritoViewModel()

        carrito.agregar(producto, 8)
        carrito.agregar(producto, 3) shouldBe false

        carrito.cantidades[1] shouldBe 8
    }

    @Test
    fun eliminarProductoDelCarrito() {
        val carrito = CarritoViewModel()

        carrito.agregar(producto, 2)
        carrito.cambiarCantidad(producto, 1, 0)

        carrito.cantidades.containsKey(1) shouldBe false
        carrito.productos.containsKey(1) shouldBe false
    }

    @Test
    fun vaciarCarritoEliminaTodo() {
        val carrito = CarritoViewModel()

        carrito.agregar(producto, 3)
        carrito.vaciar()

        carrito.cantidades.isEmpty() shouldBe true
        carrito.productos.isEmpty() shouldBe true
    }
}
