
package com.example.oylfrutossecos.feature.carrito.domain

import com.example.oylfrutossecos.data.model.ProductoCatalogo

import io.kotest.matchers.shouldBe
import org.junit.Test

class CoordinadorRevisionCarritoTest {

    private val almendras = ProductoCatalogo(
        id = 1,
        nombre = "Almendras",
        categoria = "Frutos secos",
        precio = 3000,
        stock = 10
    )

    private val nueces = ProductoCatalogo(
        id = 2,
        nombre = "Nueces",
        categoria = "Frutos secos",
        precio = 4500,
        stock = 8
    )

    // PRUEBA 1
    @Test
    fun carritoValidoNoRequiereActualizacion() {

        val resultado = CoordinadorRevisionCarrito.revisar(
            cantidades = mapOf(1 to 2),
            productosGuardados = mapOf(1 to almendras),
            productosActuales = listOf(almendras),
            validar = { _, _ -> null }
        )

        resultado.error shouldBe null
        resultado.productosActualizados shouldBe emptyMap()
    }

    // PRUEBA 2
    @Test
    fun enviaPreciosOriginalesAlValidador() {

        var validacionEjecutada = false

        CoordinadorRevisionCarrito.revisar(
            cantidades = mapOf(1 to 2),
            productosGuardados = mapOf(1 to almendras),
            productosActuales = listOf(almendras),
            validar = { cantidades, precios ->

                cantidades[1] shouldBe 2
                precios[1] shouldBe 3000

                validacionEjecutada = true
                null
            }
        )

        validacionEjecutada shouldBe true
    }

    // PRUEBA 3
    @Test
    fun precioModificadoDevuelveProductoActualizado() {

        val almendrasActualizadas = almendras.copy(
            precio = 3500
        )

        val resultado = CoordinadorRevisionCarrito.revisar(
            cantidades = mapOf(1 to 2),
            productosGuardados = mapOf(1 to almendras),
            productosActuales = listOf(almendrasActualizadas),
            validar = { _, _ ->
                "El precio cambió."
            }
        )

        resultado.error shouldBe "El precio cambió."

        resultado.productosActualizados[1]?.precio shouldBe 3500
    }

    // PRUEBA 4
    @Test
    fun stockModificadoDevuelveDatosActuales() {

        val productoActual = almendras.copy(
            stock = 1
        )

        val resultado = CoordinadorRevisionCarrito.revisar(
            cantidades = mapOf(1 to 5),
            productosGuardados = mapOf(1 to almendras),
            productosActuales = listOf(productoActual),
            validar = { _, _ ->
                "Stock insuficiente."
            }
        )

        resultado.error shouldBe "Stock insuficiente."

        resultado.productosActualizados[1]?.stock shouldBe 1
    }

    // PRUEBA 5
    @Test
    fun productoDesactivadoSeActualiza() {

        val desactivado = almendras.copy(
            activo = false
        )

        val resultado = CoordinadorRevisionCarrito.revisar(
            cantidades = mapOf(1 to 2),
            productosGuardados = mapOf(1 to almendras),
            productosActuales = listOf(desactivado),
            validar = { _, _ ->
                "Producto no disponible."
            }
        )

        resultado.productosActualizados[1]?.activo shouldBe false
    }

    // PRUEBA 6
    @Test
    fun productoEliminadoNoSeInventa() {

        val resultado = CoordinadorRevisionCarrito.revisar(
            cantidades = mapOf(1 to 2),
            productosGuardados = mapOf(1 to almendras),
            productosActuales = emptyList(),
            validar = { _, _ ->
                "El producto ya no existe."
            }
        )

        resultado.error shouldBe "El producto ya no existe."

        resultado.productosActualizados shouldBe emptyMap()
    }

    // PRUEBA 7
    @Test
    fun actualizaSoloProductosDelCarrito() {

        val resultado = CoordinadorRevisionCarrito.revisar(
            cantidades = mapOf(1 to 2),
            productosGuardados = mapOf(1 to almendras),
            productosActuales = listOf(
                almendras.copy(precio = 3500),
                nueces
            ),
            validar = { _, _ ->
                "Precio modificado."
            }
        )

        resultado.productosActualizados.keys shouldBe setOf(1)
    }

    // PRUEBA 8
    @Test
    fun carritoVacioConservaMensajeDeValidacion() {

        val resultado = CoordinadorRevisionCarrito.revisar(
            cantidades = emptyMap(),
            productosGuardados = emptyMap(),
            productosActuales = listOf(almendras),
            validar = { _, _ ->
                "El carrito está vacío."
            }
        )

        resultado.error shouldBe "El carrito está vacío."

        resultado.productosActualizados shouldBe emptyMap()
    }
}
