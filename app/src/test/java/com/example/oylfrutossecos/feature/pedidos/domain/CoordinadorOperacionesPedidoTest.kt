
package com.example.oylfrutossecos.feature.pedidos.domain

import com.example.oylfrutossecos.data.model.DetallePedido
import com.example.oylfrutossecos.data.model.Pedido
import com.example.oylfrutossecos.data.model.ProductoCatalogo

import io.kotest.matchers.shouldBe
import org.junit.Test

class CoordinadorOperacionesPedidoTest {

    private val almendras = ProductoCatalogo(
        id = 1,
        nombre = "Almendras",
        categoria = "Frutos secos",
        precio = 3000,
        stock = 20
    )

    private fun crearPedido(
        detalles: List<DetallePedido>
    ): Pedido {

        return Pedido(
            numero = "OYL-P-0001",
            fecha = 123456789L,
            clienteId = 7,
            nombreCliente = "Ana Gomez",
            telefono = "912345678",
            modalidad = "Retiro",
            direccion = "",
            productos = detalles,
            total = 6000
        )
    }

    // PRUEBA 1: Registro exitoso
    @Test
    fun confirmaPedidoCorrectamente() {

        val resultado = CoordinadorOperacionesPedido.confirmar(
            validarCarrito = { null },
            productos = listOf(almendras),
            cantidades = mapOf(1 to 2),
            registrar = { detalles ->
                crearPedido(detalles)
            }
        )

        val esperado = crearPedido(
            listOf(
                DetallePedido(
                    idProducto = 1,
                    nombre = "Almendras",
                    cantidad = 2,
                    precioUnitario = 3000
                )
            )
        )

        resultado shouldBe ResultadoConfirmacionPedido.Registrado(
            esperado
        )
    }

    // PRUEBA 2: Carrito inválido
    @Test
    fun rechazaCarritoInvalido() {

        var intentoRegistro = false

        val resultado = CoordinadorOperacionesPedido.confirmar(
            validarCarrito = {
                "Stock insuficiente"
            },
            productos = listOf(almendras),
            cantidades = mapOf(1 to 2),
            registrar = {
                intentoRegistro = true
                crearPedido(it)
            }
        )

        resultado shouldBe
                ResultadoConfirmacionPedido.CarritoInvalido(
                    "Stock insuficiente"
                )

        intentoRegistro shouldBe false
    }

    // PRUEBA 3: Error al guardar
    @Test
    fun devuelveErrorCuandoFallaRegistro() {

        val resultado = CoordinadorOperacionesPedido.confirmar(
            validarCarrito = { null },
            productos = listOf(almendras),
            cantidades = mapOf(1 to 2),
            registrar = {
                throw IllegalStateException("Error de almacenamiento")
            }
        )

        resultado shouldBe ResultadoConfirmacionPedido.ErrorGuardado
    }

    // PRUEBA 4: Preparar cantidades
    @Test
    fun enviaSoloProductosConCantidadPositiva() {

        val resultado = CoordinadorOperacionesPedido.confirmar(
            validarCarrito = { null },
            productos = listOf(almendras),
            cantidades = mapOf(1 to 0),
            registrar = { detalles ->
                detalles shouldBe emptyList()
                crearPedido(detalles)
            }
        )

        (resultado is ResultadoConfirmacionPedido.Registrado) shouldBe true    }

    // PRUEBA 5: Cambiar estado correctamente
    @Test
    fun cambiaEstadoCorrectamente() {

        var ejecutado = false

        val resultado = CoordinadorOperacionesPedido.cambiarEstado {
            ejecutado = true
        }

        resultado shouldBe null
        ejecutado shouldBe true
    }

    // PRUEBA 6: Error al cambiar estado
    @Test
    fun devuelveErrorSiNoPuedeCambiarEstado() {

        val resultado = CoordinadorOperacionesPedido.cambiarEstado {
            throw IllegalArgumentException("Estado no permitido")
        }

        resultado shouldBe
                "No se pudo actualizar el estado del pedido."
    }
}
