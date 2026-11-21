
package com.example.oylfrutossecos.feature.carrito.domain

import com.example.oylfrutossecos.data.model.ProductoCatalogo
import io.kotest.matchers.shouldBe
import org.junit.Test

class CalculadoraCarritoTest {

    private fun producto(
        id: Int = 1,
        precio: Int = 3000,
        stock: Int = 10,
        activo: Boolean = true
    ) = ProductoCatalogo(
        id = id,
        nombre = "Almendras",
        categoria = "Frutos secos",
        precio = precio,
        stock = stock,
        activo = activo
    )

    // PRUEBA 1: Total de un producto
    @Test
    fun totalUnProducto() {
        val p = producto()

        CalculadoraCarrito.calcularTotal(
            listOf(p),
            mapOf(1 to p),
            mapOf(1 to 2)
        ) shouldBe 6000L
    }

    // PRUEBA 2: Total de varios productos
    @Test
    fun totalVariosProductos() {
        val p1 = producto(id = 1, precio = 3000)
        val p2 = producto(id = 2, precio = 2000)

        CalculadoraCarrito.calcularTotal(
            listOf(p1, p2),
            mapOf(1 to p1, 2 to p2),
            mapOf(1 to 2, 2 to 3)
        ) shouldBe 12000L
    }

    // PRUEBA 3: Carrito vacío
    @Test
    fun totalCarritoVacio() {
        CalculadoraCarrito.calcularTotal(
            emptyList(),
            emptyMap(),
            emptyMap()
        ) shouldBe 0L
    }

    // PRUEBA 4: Utiliza el precio actualizado
    @Test
    fun totalConPrecioActualizado() {
        val anterior = producto(precio = 3000)
        val actual = producto(precio = 3500)

        CalculadoraCarrito.calcularTotal(
            listOf(anterior),
            mapOf(1 to actual),
            mapOf(1 to 2)
        ) shouldBe 7000L
    }

    // PRUEBA 5: Detecta cambio de precio
    @Test
    fun detectarCambioPrecio() {
        val anterior = producto(precio = 3000)
        val actual = producto(precio = 3500)

        CalculadoraCarrito.requiereRevision(
            listOf(anterior),
            mapOf(1 to actual),
            mapOf(1 to 2)
        ) shouldBe true
    }

    // PRUEBA 6: Detecta stock insuficiente
    @Test
    fun detectarStockInsuficiente() {
        val anterior = producto(stock = 10)
        val actual = producto(stock = 2)

        CalculadoraCarrito.requiereRevision(
            listOf(anterior),
            mapOf(1 to actual),
            mapOf(1 to 5)
        ) shouldBe true
    }

    // PRUEBA 7: Detecta producto desactivado
    @Test
    fun detectarProductoDesactivado() {
        val anterior = producto()
        val actual = producto(activo = false)

        CalculadoraCarrito.requiereRevision(
            listOf(anterior),
            mapOf(1 to actual),
            mapOf(1 to 2)
        ) shouldBe true
    }

    // PRUEBA 8: Detecta producto eliminado
    @Test
    fun detectarProductoEliminado() {
        val anterior = producto()

        CalculadoraCarrito.requiereRevision(
            listOf(anterior),
            emptyMap(),
            mapOf(1 to 2)
        ) shouldBe true
    }

    // PRUEBA 9: No requiere revisión si está correcto
    @Test
    fun carritoSinCambios() {
        val p = producto()

        CalculadoraCarrito.requiereRevision(
            listOf(p),
            mapOf(1 to p),
            mapOf(1 to 2)
        ) shouldBe false
    }
}
