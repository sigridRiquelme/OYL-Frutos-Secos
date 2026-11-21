
package com.example.oylfrutossecos.ui.viewmodel

import com.example.oylfrutossecos.data.local.entity.ProductoEntity
import com.example.oylfrutossecos.data.repository.ProductoRepository
import com.example.oylfrutossecos.feature.productos.domain.DatosNuevoProducto

import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductoViewModelTest {

    // Repositorio simulado mediante MockK.
    private val repository = mockk<ProductoRepository>()

    private val datosValidos = DatosNuevoProducto(
        nombre = "Nueces",
        categoria = "Frutos secos",
        precio = "3500",
        stock = "20",
        descripcion = "Nueces naturales",
        presentacion = "Bolsa 250 g",
        imagenUri = null
    )

    // Reemplazar el hilo principal de Android.
    @Before
    fun prepararCorrutinas() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    // Restaurar el hilo principal después de cada prueba.
    @After
    fun restaurarCorrutinas() {
        Dispatchers.resetMain()
    }

    // Crear ViewModel sin base de datos real.
    private fun crearViewModel(): ProductoViewModel {

        every {
            repository.productos
        } returns flowOf(emptyList<ProductoEntity>())

        return ProductoViewModel(repository)
    }

    // Simular una creación exitosa.
    private fun simularCreacionCorrecta() {

        coEvery {
            repository.crearProducto(
                nombre = "Nueces",
                categoria = "Frutos secos",
                precio = 3500,
                stock = 20,
                descripcion = "Nueces naturales",
                presentacion = "Bolsa 250 g",
                imagenUri = null
            )
        } just Runs
    }

    // Simular una edición exitosa.
    private fun simularEdicionCorrecta() {

        coEvery {
            repository.editarProducto(
                id = 7,
                nombre = "Nueces",
                categoria = "Frutos secos",
                precio = 3500,
                stock = 20,
                descripcion = "Nueces naturales",
                presentacion = "Bolsa 250 g",
                imagenUri = null
            )
        } just Runs
    }

    // PRUEBA 1: Crear producto correctamente
    @Test
    fun crearProductoCorrectamente() = runTest {

        simularCreacionCorrecta()

        val viewModel = crearViewModel()
        var resultado: Boolean? = null

        viewModel.crearProducto(datosValidos) {
            resultado = it
        }

        advanceUntilIdle()

        resultado shouldBe true

        coVerify(exactly = 1) {
            repository.crearProducto(
                "Nueces", "Frutos secos",
                3500, 20,
                "Nueces naturales", "Bolsa 250 g",
                null
            )
        }
    }

    // PRUEBA 2: No crear con datos inválidos
    @Test
    fun noCrearProductoInvalido() = runTest {

        val viewModel = crearViewModel()
        var resultado: Boolean? = null

        viewModel.crearProducto(
            datosValidos.copy(precio = "0")
        ) {
            resultado = it
        }

        resultado shouldBe false

        coVerify(exactly = 0) {
            repository.crearProducto(
                any(), any(), any(), any(),
                any(), any(), any()
            )
        }
    }

    // PRUEBA 3: Error del repositorio al crear
    @Test
    fun errorAlCrearProducto() = runTest {

        coEvery {
            repository.crearProducto(
                "Nueces", "Frutos secos",
                3500, 20,
                "Nueces naturales", "Bolsa 250 g",
                null
            )
        } throws IllegalStateException("Error de base de datos")

        val viewModel = crearViewModel()
        var resultado: Boolean? = null

        viewModel.crearProducto(datosValidos) {
            resultado = it
        }

        advanceUntilIdle()

        resultado shouldBe false
    }

    // PRUEBA 4: Editar producto correctamente
    @Test
    fun editarProductoCorrectamente() = runTest {

        simularEdicionCorrecta()

        val viewModel = crearViewModel()
        var resultado: Boolean? = null

        viewModel.editarProducto(
            id = 7,
            datos = datosValidos
        ) {
            resultado = it
        }

        advanceUntilIdle()

        resultado shouldBe true

        coVerify(exactly = 1) {
            repository.editarProducto(
                7, "Nueces", "Frutos secos",
                3500, 20,
                "Nueces naturales", "Bolsa 250 g",
                null
            )
        }
    }

    // PRUEBA 5: No editar con datos inválidos
    @Test
    fun noEditarProductoInvalido() = runTest {

        val viewModel = crearViewModel()
        var resultado: Boolean? = null

        viewModel.editarProducto(
            id = 7,
            datos = datosValidos.copy(nombre = "A")
        ) {
            resultado = it
        }

        resultado shouldBe false

        coVerify(exactly = 0) {
            repository.editarProducto(
                any(), any(), any(), any(),
                any(), any(), any(), any()
            )
        }
    }

    // PRUEBA 6: Error del repositorio al editar
    @Test
    fun errorAlEditarProducto() = runTest {

        coEvery {
            repository.editarProducto(
                7, "Nueces", "Frutos secos",
                3500, 20,
                "Nueces naturales", "Bolsa 250 g",
                null
            )
        } throws IllegalStateException("Error al actualizar")

        val viewModel = crearViewModel()
        var resultado: Boolean? = null

        viewModel.editarProducto(7, datosValidos) {
            resultado = it
        }

        advanceUntilIdle()

        resultado shouldBe false
    }

    // PRUEBA 7: Activar producto
    @Test
    fun activarProductoCorrectamente() = runTest {

        coEvery {
            repository.cambiarEstadoProducto(7, true)
        } just Runs

        val viewModel = crearViewModel()
        var resultado: Boolean? = null

        viewModel.cambiarEstadoProducto(7, true) {
            resultado = it
        }

        advanceUntilIdle()

        resultado shouldBe true

        coVerify(exactly = 1) {
            repository.cambiarEstadoProducto(7, true)
        }
    }

    // PRUEBA 8: Desactivar producto
    @Test
    fun desactivarProductoCorrectamente() = runTest {

        coEvery {
            repository.cambiarEstadoProducto(7, false)
        } just Runs

        val viewModel = crearViewModel()
        var resultado: Boolean? = null

        viewModel.cambiarEstadoProducto(7, false) {
            resultado = it
        }

        advanceUntilIdle()

        resultado shouldBe true

        coVerify(exactly = 1) {
            repository.cambiarEstadoProducto(7, false)
        }
    }

    // PRUEBA 9: Error al cambiar estado
    @Test
    fun errorAlCambiarEstado() = runTest {

        coEvery {
            repository.cambiarEstadoProducto(7, true)
        } throws IllegalStateException("Producto inexistente")

        val viewModel = crearViewModel()
        var resultado: Boolean? = null

        viewModel.cambiarEstadoProducto(7, true) {
            resultado = it
        }

        advanceUntilIdle()

        resultado shouldBe false
    }

    // PRUEBA 10: Entrada de inventario
    @Test
    fun registrarEntradaDeStock() = runTest {

        coEvery {
            repository.ajustarStock(7, 10)
        } just Runs

        val viewModel = crearViewModel()
        var error: String? = "Sin ejecutar"

        viewModel.ajustarStock(
            id = 7,
            cantidadTexto = "10",
            esEntrada = true
        ) {
            error = it
        }

        advanceUntilIdle()

        error shouldBe null

        coVerify(exactly = 1) {
            repository.ajustarStock(7, 10)
        }
    }

    // PRUEBA 11: Salida de inventario
    @Test
    fun registrarSalidaDeStock() = runTest {

        coEvery {
            repository.ajustarStock(7, -5)
        } just Runs

        val viewModel = crearViewModel()
        var error: String? = "Sin ejecutar"

        viewModel.ajustarStock(
            id = 7,
            cantidadTexto = "5",
            esEntrada = false
        ) {
            error = it
        }

        advanceUntilIdle()

        error shouldBe null

        coVerify(exactly = 1) {
            repository.ajustarStock(7, -5)
        }
    }

    // PRUEBA 12: Rechazar cantidad inválida
    @Test
    fun noAjustarStockConCantidadInvalida() = runTest {

        val viewModel = crearViewModel()
        var error: String? = null

        viewModel.ajustarStock(
            id = 7,
            cantidadTexto = "abc",
            esEntrada = true
        ) {
            error = it
        }

        error shouldBe "Ingresa una cantidad entera válida"

        coVerify(exactly = 0) {
            repository.ajustarStock(any(), any())
        }
    }

    // PRUEBA 13: Error al guardar ajuste
    @Test
    fun errorAlGuardarAjusteStock() = runTest {

        coEvery {
            repository.ajustarStock(7, -50)
        } throws IllegalStateException("Stock insuficiente")

        val viewModel = crearViewModel()
        var error: String? = null

        viewModel.ajustarStock(
            id = 7,
            cantidadTexto = "50",
            esEntrada = false
        ) {
            error = it
        }

        advanceUntilIdle()

        error shouldBe
                "No se pudo ajustar el stock. " +
                "Verifica las existencias disponibles."
    }
}
