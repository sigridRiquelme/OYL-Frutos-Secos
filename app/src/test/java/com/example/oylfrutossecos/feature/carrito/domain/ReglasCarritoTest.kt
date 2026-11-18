
package com.example.oylfrutossecos.feature.carrito.domain

import com.example.oylfrutossecos.data.model.ProductoCatalogo
import io.kotest.matchers.shouldBe
import org.junit.Test

class ReglasCarritoTest {

    private fun producto(
        stock: Int = 10,
        activo: Boolean = true
    ) = ProductoCatalogo(
        id = 1,
        nombre = "Almendras",
        categoria = "Frutos secos",
        precio = 3000,
        stock = stock,
        activo = activo
    )

    @Test
    fun agregarCantidadValida() {
        ReglasCarrito.puedeAgregar(
            producto(),
            cantidadSolicitada = 2,
            cantidadActual = 3
        ) shouldBe true
    }

    @Test
    fun noAgregarProductoInactivo() {
        ReglasCarrito.puedeAgregar(
            producto(activo = false),
            cantidadSolicitada = 2,
            cantidadActual = 0
        ) shouldBe false
    }

    @Test
    fun noAgregarCantidadCero() {
        ReglasCarrito.puedeAgregar(
            producto(),
            cantidadSolicitada = 0,
            cantidadActual = 0
        ) shouldBe false
    }

    @Test
    fun noAgregarCantidadNegativa() {
        ReglasCarrito.puedeAgregar(
            producto(),
            cantidadSolicitada = -2,
            cantidadActual = 0
        ) shouldBe false
    }

    @Test
    fun noAgregarSinStock() {
        ReglasCarrito.puedeAgregar(
            producto(stock = 0),
            cantidadSolicitada = 1,
            cantidadActual = 0
        ) shouldBe false
    }

    @Test
    fun noSuperarStockDisponible() {
        ReglasCarrito.puedeAgregar(
            producto(stock = 10),
            cantidadSolicitada = 5,
            cantidadActual = 8
        ) shouldBe false
    }

    @Test
    fun agregarHastaLimiteStock() {
        ReglasCarrito.puedeAgregar(
            producto(stock = 10),
            cantidadSolicitada = 2,
            cantidadActual = 8
        ) shouldBe true
    }

    @Test
    fun aumentarCantidadDentroDelStock() {
        ReglasCarrito.puedeCambiarCantidad(
            producto(stock = 10),
            cantidadAnterior = 2,
            nuevaCantidad = 5
        ) shouldBe true
    }

    @Test
    fun impedirAumentoSobreStock() {
        ReglasCarrito.puedeCambiarCantidad(
            producto(stock = 5),
            cantidadAnterior = 2,
            nuevaCantidad = 8
        ) shouldBe false
    }

    @Test
    fun permitirDisminuirCantidad() {
        ReglasCarrito.puedeCambiarCantidad(
            producto(stock = 5),
            cantidadAnterior = 10,
            nuevaCantidad = 8
        ) shouldBe true
    }

    @Test
    fun noModificarProductoInactivo() {
        ReglasCarrito.puedeCambiarCantidad(
            producto(activo = false),
            cantidadAnterior = 2,
            nuevaCantidad = 3
        ) shouldBe false
    }

    @Test
    fun noModificarProductoInexistente() {
        ReglasCarrito.puedeCambiarCantidad(
            producto = null,
            cantidadAnterior = 2,
            nuevaCantidad = 3
        ) shouldBe false
    }
}
