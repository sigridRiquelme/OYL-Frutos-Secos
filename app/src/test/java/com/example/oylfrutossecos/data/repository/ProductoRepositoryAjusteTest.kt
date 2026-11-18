
package com.example.oylfrutossecos.data.repository

import com.example.oylfrutossecos.data.local.dao.ProductoDao
import com.example.oylfrutossecos.data.local.entity.ProductoEntity
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ProductoRepositoryAjusteTest {

    // DAO simulado: no utiliza la base de datos real
    private val dao = mockk<ProductoDao>()

    private fun crearRepositorio(): ProductoRepository {

        // El repositorio observa los productos al crearse
        every {
            dao.obtenerProductos()
        } returns flowOf(emptyList<ProductoEntity>())

        return ProductoRepository(dao)
    }

    // PRUEBA 1: El repositorio guarda una entrada
    @Test
    fun registrarEntradaCorrectamente() = runTest {

        coEvery {
            dao.ajustarStock(1, 5)
        } returns 1

        val repository = crearRepositorio()

        repository.ajustarStock(
            id = 1,
            variacion = 5
        )

        coVerify(exactly = 1) {
            dao.ajustarStock(1, 5)
        }
    }

    // PRUEBA 2: Una salida rechazada genera error
    @Test
    fun salidaRechazadaGeneraError() = runTest {

        coEvery {
            dao.ajustarStock(1, -20)
        } returns 0

        val repository = crearRepositorio()

        shouldThrow<IllegalStateException> {
            repository.ajustarStock(
                id = 1,
                variacion = -20
            )
        }
    }

    // PRUEBA 3: No permitir variación cero
    @Test
    fun noPermitirVariacionCero() = runTest {

        val repository = crearRepositorio()

        shouldThrow<IllegalArgumentException> {
            repository.ajustarStock(
                id = 1,
                variacion = 0
            )
        }

        coVerify(exactly = 0) {
            dao.ajustarStock(any(), any())
        }
    }
}
