
package com.example.oylfrutossecos

import com.example.oylfrutossecos.data.local.entity.ProductoEntity
import com.example.oylfrutossecos.ui.validation.ValidadorCarrito

import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorCarritoTest {

    // Producto ficticio para nuestras pruebas
    private fun producto(
        id: Int = 1,
        precio: Int = 3000,
        stock: Int = 10,
        activo: Boolean = true
    ): ProductoEntity {

        return ProductoEntity(
            id = id,
            nombre = "Almendras",
            categoria = "Frutos secos",
            precio = precio,
            stock = stock,
            descripcion = "Producto de prueba",
            presentacion = "250 g",
            activo = activo
        )
    }

    @Test
    fun compraValida() {

        val error = ValidadorCarrito.validar(
            cantidades = mapOf(1 to 2),
            preciosCarrito = mapOf(1 to 3000),
            productosActuales = listOf(producto())
        )

        assertNull(error)
    }

    @Test
    fun productoDesactivado() {

        val error = ValidadorCarrito.validar(
            cantidades = mapOf(1 to 2),
            preciosCarrito = mapOf(1 to 3000),
            productosActuales = listOf(
                producto(activo = false)
            )
        )

        assertTrue(error?.contains("ya no está disponible") == true)
    }

    @Test
    fun stockInsuficiente() {

        val error = ValidadorCarrito.validar(
            cantidades = mapOf(1 to 5),
            preciosCarrito = mapOf(1 to 3000),
            productosActuales = listOf(
                producto(stock = 2)
            )
        )

        assertTrue(error?.contains("Stock insuficiente") == true)
    }

    @Test
    fun precioModificado() {

        val error = ValidadorCarrito.validar(
            cantidades = mapOf(1 to 2),
            preciosCarrito = mapOf(1 to 3000),
            productosActuales = listOf(
                producto(precio = 3500)
            )
        )

        assertTrue(error?.contains("precio") == true)
    }

    @Test
    fun productoEliminado() {

        val error = ValidadorCarrito.validar(
            cantidades = mapOf(1 to 2),
            preciosCarrito = mapOf(1 to 3000),
            productosActuales = emptyList()
        )

        assertTrue(error?.contains("ya no existe") == true)
    }

    @Test
    fun carritoVacio() {

        val error = ValidadorCarrito.validar(
            cantidades = emptyMap(),
            preciosCarrito = emptyMap(),
            productosActuales = listOf(producto())
        )

        assertTrue(error?.contains("vacío") == true)
    }

    @Test
    fun cantidadInvalida() {

        val error = ValidadorCarrito.validar(
            cantidades = mapOf(1 to 0),
            preciosCarrito = mapOf(1 to 3000),
            productosActuales = listOf(producto())
        )

        assertTrue(error?.contains("cantidad inválida") == true)
    }

    @Test
    fun totalDemasiadoAlto() {

        val error = ValidadorCarrito.validar(
            cantidades = mapOf(1 to 3),
            preciosCarrito = mapOf(1 to 1000000000),
            productosActuales = listOf(
                producto(
                    precio = 1000000000,
                    stock = 10
                )
            )
        )

        assertTrue(error?.contains("supera") == true)
    }
}
