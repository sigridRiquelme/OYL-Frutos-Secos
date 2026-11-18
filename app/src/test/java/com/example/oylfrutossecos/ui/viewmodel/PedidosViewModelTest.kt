
package com.example.oylfrutossecos.ui.viewmodel

import android.content.Context

import com.example.oylfrutossecos.data.local.DetallePedidoLocal
import com.example.oylfrutossecos.data.local.PedidoLocal
import com.example.oylfrutossecos.feature.pedidos.data.PedidosRepository

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

import org.junit.Test

class PedidosViewModelTest {

    // Repositorio simulado
    private val repository = mockk<PedidosRepository>()

    // Contexto simulado: no accedemos a Android real
    private val context = mockk<Context>()

    private val producto = DetallePedidoLocal(
        idProducto = 1,
        nombre = "Almendras",
        cantidad = 2,
        precioUnitario = 3000
    )

    private val pedido = PedidoLocal(
        numero = "OYL-P-0001",
        fecha = 123456789L,
        clienteId = 7,
        nombreCliente = "Ana Gomez",
        telefono = "912345678",
        modalidad = "Retiro",
        direccion = "",
        productos = listOf(producto),
        total = 6000,
        estado = "Pendiente"
    )

    private fun crearViewModel(): PedidosViewModel {
        return PedidosViewModel(repository)
    }

    // PRUEBA 1: Cargar pedidos existentes
    @Test
    fun cargarPedidosCorrectamente() {

        every {
            repository.cargar(context)
        } returns listOf(pedido)

        val viewModel = crearViewModel()

        viewModel.cargar(context)

        viewModel.pedidos shouldBe listOf(pedido)

        verify(exactly = 1) {
            repository.cargar(context)
        }
    }

    // PRUEBA 2: Cargar lista vacía
    @Test
    fun cargarPedidosVacios() {

        every {
            repository.cargar(context)
        } returns emptyList()

        val viewModel = crearViewModel()

        viewModel.cargar(context)

        viewModel.pedidos.isEmpty() shouldBe true
    }

    // PRUEBA 3: Registrar pedido correctamente
    @Test
    fun registrarPedidoCorrectamente() {

        every {
            repository.registrar(
                context,
                7,
                "Ana Gomez",
                "912345678",
                "Retiro",
                "",
                listOf(producto)
            )
        } returns pedido

        val viewModel = crearViewModel()

        val resultado = viewModel.registrar(
            context = context,
            clienteId = 7,
            nombreCliente = "Ana Gomez",
            telefono = "912345678",
            modalidad = "Retiro",
            direccion = "",
            productos = listOf(producto)
        )

        resultado shouldBe pedido

        viewModel.pedidos shouldBe listOf(pedido)

        verify(exactly = 1) {
            repository.registrar(
                context,
                7,
                "Ana Gomez",
                "912345678",
                "Retiro",
                "",
                listOf(producto)
            )
        }
    }

    // PRUEBA 4: No perder pedidos anteriores
    @Test
    fun agregarPedidoMantieneLosAnteriores() {

        val segundoPedido = pedido.copy(
            numero = "OYL-P-0002"
        )

        every {
            repository.cargar(context)
        } returns listOf(pedido)

        every {
            repository.registrar(
                context,
                7,
                "Ana Gomez",
                "912345678",
                "Retiro",
                "",
                listOf(producto)
            )
        } returns segundoPedido

        val viewModel = crearViewModel()

        viewModel.cargar(context)

        viewModel.registrar(
            context,
            7,
            "Ana Gomez",
            "912345678",
            "Retiro",
            "",
            listOf(producto)
        )

        viewModel.pedidos shouldBe listOf(
            pedido,
            segundoPedido
        )
    }

    // PRUEBA 5: Actualizar estado de pedido
    @Test
    fun actualizarEstadoCorrectamente() {

        val actualizado = pedido.copy(
            estado = "En preparación"
        )

        every {
            repository.cargar(context)
        } returns listOf(pedido)

        every {
            repository.actualizarEstado(
                context,
                "OYL-P-0001",
                "En preparación"
            )
        } returns listOf(actualizado)

        val viewModel = crearViewModel()

        viewModel.cargar(context)

        viewModel.actualizarEstado(
            context,
            "OYL-P-0001",
            "En preparación"
        )

        viewModel.pedidos shouldBe listOf(actualizado)

        verify(exactly = 1) {
            repository.actualizarEstado(
                context,
                "OYL-P-0001",
                "En preparación"
            )
        }
    }

    // PRUEBA 6: Error al cargar pedidos
    @Test
    fun errorAlCargarNoEliminaPedidosExistentes() {

        every {
            repository.cargar(context)
        } returns listOf(pedido)

        val viewModel = crearViewModel()

        viewModel.cargar(context)

        // Simulamos un fallo en una nueva carga
        every {
            repository.cargar(context)
        } throws IllegalStateException(
            "Error de almacenamiento"
        )

        shouldThrow<IllegalStateException> {
            viewModel.cargar(context)
        }

        viewModel.pedidos shouldBe listOf(pedido)
    }

    // PRUEBA 7: Error al registrar pedido
    @Test
    fun errorAlRegistrarNoAgregaPedido() {

        every {
            repository.registrar(
                context,
                7,
                "Ana Gomez",
                "912345678",
                "Retiro",
                "",
                listOf(producto)
            )
        } throws IllegalStateException(
            "No se pudo guardar"
        )

        val viewModel = crearViewModel()

        shouldThrow<IllegalStateException> {
            viewModel.registrar(
                context,
                7,
                "Ana Gomez",
                "912345678",
                "Retiro",
                "",
                listOf(producto)
            )
        }

        viewModel.pedidos.isEmpty() shouldBe true
    }

    // PRUEBA 8: Error al actualizar estado
    @Test
    fun errorAlActualizarMantienePedidoOriginal() {

        every {
            repository.cargar(context)
        } returns listOf(pedido)

        every {
            repository.actualizarEstado(
                context,
                "OYL-P-0001",
                "En preparación"
            )
        } throws IllegalStateException(
            "Cambio rechazado"
        )

        val viewModel = crearViewModel()

        viewModel.cargar(context)

        shouldThrow<IllegalStateException> {
            viewModel.actualizarEstado(
                context,
                "OYL-P-0001",
                "En preparación"
            )
        }

        viewModel.pedidos shouldBe listOf(pedido)
    }
}
