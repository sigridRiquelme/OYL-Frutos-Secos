
package com.example.oylfrutossecos.data.repository

import com.example.oylfrutossecos.data.local.dao.ProductoDao
import com.example.oylfrutossecos.data.local.entity.ProductoEntity

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

import org.junit.Test

class ProductoRepositoryTest {

    // DAO simulado: no utiliza Room realmente
    private val dao = mockk<ProductoDao>()

    // Producto existente para nuestras pruebas
    private val productoExistente = ProductoEntity(
        id = 7,
        nombre = "Almendras",
        categoria = "Frutos secos",
        precio = 3500,
        stock = 15,
        descripcion = "Almendras naturales",
        presentacion = "Bolsa 250 g",
        imagenUri = "content://foto-original",
        activo = false
    )

    // Preparar el repositorio con el DAO simulado
    private fun crearRepositorio(): ProductoRepository {

        every {
            dao.obtenerProductos()
        } returns flowOf(emptyList<ProductoEntity>())

        return ProductoRepository(dao)
    }

    // Datos reutilizables para crear productos
    private suspend fun crearProducto(
        repository: ProductoRepository
    ) {
        repository.crearProducto(
            nombre = "Nueces",
            categoria = "Frutos secos",
            precio = 4500,
            stock = 20,
            descripcion = "Nueces naturales",
            presentacion = "Bolsa 500 g",
            imagenUri = null
        )
    }

    // Datos reutilizables para editar productos
    private suspend fun editarProducto(
        repository: ProductoRepository
    ) {
        repository.editarProducto(
            id = 7,
            nombre = "Almendras premium",
            categoria = "Frutos secos",
            precio = 5000,
            stock = 25,
            descripcion = "Almendras seleccionadas",
            presentacion = "Bolsa 500 g",
            imagenUri = "content://foto-nueva"
        )
    }

    // PRUEBA 1: Crear producto correctamente
    @Test
    fun crearProductoCorrectamente() = runTest {

        coEvery {
            dao.obtenerSiguienteId()
        } returns 11

        coEvery {
            dao.insertarProducto(any())
        } just Runs

        val repository = crearRepositorio()

        crearProducto(repository)

        val esperado = ProductoEntity(
            id = 11,
            nombre = "Nueces",
            categoria = "Frutos secos",
            precio = 4500,
            stock = 20,
            descripcion = "Nueces naturales",
            presentacion = "Bolsa 500 g",
            imagenUri = null,
            activo = true
        )

        coVerify(exactly = 1) {
            dao.obtenerSiguienteId()
        }

        coVerify(exactly = 1) {
            dao.insertarProducto(esperado)
        }
    }

    // PRUEBA 2: Error al crear producto
    @Test
    fun errorAlCrearProducto() = runTest {

        coEvery {
            dao.obtenerSiguienteId()
        } returns 11

        coEvery {
            dao.insertarProducto(any())
        } throws IllegalStateException("Error de base de datos")

        val repository = crearRepositorio()

        val error = shouldThrow<IllegalStateException> {
            crearProducto(repository)
        }

        error.message shouldBe "Error de base de datos"
    }

    // PRUEBA 3: Editar producto correctamente
    @Test
    fun editarProductoCorrectamente() = runTest {

        coEvery {
            dao.obtenerProductoPorId(7)
        } returns productoExistente

        coEvery {
            dao.actualizarProducto(any())
        } just Runs

        val repository = crearRepositorio()

        editarProducto(repository)

        val esperado = productoExistente.copy(
            nombre = "Almendras premium",
            categoria = "Frutos secos",
            precio = 5000,
            stock = 25,
            descripcion = "Almendras seleccionadas",
            presentacion = "Bolsa 500 g",
            imagenUri = "content://foto-nueva"
        )

        // Verifica los cambios y que conserve
        // su ID y su estado inactivo.
        coVerify(exactly = 1) {
            dao.actualizarProducto(esperado)
        }

        esperado.id shouldBe 7
        esperado.activo shouldBe false
    }

    // PRUEBA 4: No editar un producto inexistente
    @Test
    fun editarProductoInexistente() = runTest {

        coEvery {
            dao.obtenerProductoPorId(7)
        } returns null

        val repository = crearRepositorio()

        shouldThrow<IllegalArgumentException> {
            editarProducto(repository)
        }

        coVerify(exactly = 0) {
            dao.actualizarProducto(any())
        }
    }

    // PRUEBA 5: Error de base de datos al editar
    @Test
    fun errorAlEditarProducto() = runTest {

        coEvery {
            dao.obtenerProductoPorId(7)
        } returns productoExistente

        coEvery {
            dao.actualizarProducto(any())
        } throws IllegalStateException("No se pudo guardar")

        val repository = crearRepositorio()

        val error = shouldThrow<IllegalStateException> {
            editarProducto(repository)
        }

        error.message shouldBe "No se pudo guardar"
    }

    // PRUEBA 6: Activar producto
    @Test
    fun activarProductoCorrectamente() = runTest {

        coEvery {
            dao.cambiarEstadoProducto(7, true)
        } returns 1

        val repository = crearRepositorio()

        repository.cambiarEstadoProducto(
            id = 7,
            activo = true
        )

        coVerify(exactly = 1) {
            dao.cambiarEstadoProducto(7, true)
        }
    }

    // PRUEBA 7: Desactivar producto
    @Test
    fun desactivarProductoCorrectamente() = runTest {

        coEvery {
            dao.cambiarEstadoProducto(7, false)
        } returns 1

        val repository = crearRepositorio()

        repository.cambiarEstadoProducto(
            id = 7,
            activo = false
        )

        coVerify(exactly = 1) {
            dao.cambiarEstadoProducto(7, false)
        }
    }

    // PRUEBA 8: Cambiar estado de producto inexistente
    @Test
    fun cambiarEstadoProductoInexistente() = runTest {

        coEvery {
            dao.cambiarEstadoProducto(7, true)
        } returns 0

        val repository = crearRepositorio()

        shouldThrow<IllegalArgumentException> {
            repository.cambiarEstadoProducto(
                id = 7,
                activo = true
            )
        }
    }

    // PRUEBA 9: Consultar producto por ID
    @Test
    fun buscarProductoPorId() = runTest {

        coEvery {
            dao.obtenerProductoPorId(7)
        } returns productoExistente

        val repository = crearRepositorio()

        val resultado = repository.obtenerProductoPorId(7)

        resultado shouldBe productoExistente

        coVerify(exactly = 1) {
            dao.obtenerProductoPorId(7)
        }
    }

    // PRUEBA 10: Guardar producto
    @Test
    fun guardarProductoCorrectamente() = runTest {

        coEvery {
            dao.guardarProducto(productoExistente)
        } just Runs

        val repository = crearRepositorio()

        repository.guardarProducto(productoExistente)

        coVerify(exactly = 1) {
            dao.guardarProducto(productoExistente)
        }
    }

    // PRUEBA 11: Actualizar producto existente
    @Test
    fun actualizarProductoCorrectamente() = runTest {

        val modificado = productoExistente.copy(
            precio = 6000
        )

        coEvery {
            dao.actualizarProducto(modificado)
        } just Runs

        val repository = crearRepositorio()

        repository.actualizarProducto(modificado)

        coVerify(exactly = 1) {
            dao.actualizarProducto(modificado)
        }
    }

    // PRUEBA 12: Eliminar producto
    @Test
    fun eliminarProductoCorrectamente() = runTest {

        coEvery {
            dao.eliminarProducto(productoExistente)
        } just Runs

        val repository = crearRepositorio()

        repository.eliminarProducto(productoExistente)

        coVerify(exactly = 1) {
            dao.eliminarProducto(productoExistente)
        }
    }
}
