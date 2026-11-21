package com.example.oylfrutossecos.data.local

import com.example.oylfrutossecos.data.local.dao.ProductoDao
import com.example.oylfrutossecos.data.local.entity.ProductoEntity
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

class DatosInicialesTest {
    @Test fun inicializaSoloBaseVaciaConProductosValidos() = runTest {
        val bd = mockk<AppDatabase>()
        val dao = mockk<ProductoDao>()
        every { bd.productoDao() } returns dao
        coEvery { dao.contarProductos() } returns 0
        val guardados = slot<List<ProductoEntity>>()
        coEvery { dao.guardarProductos(capture(guardados)) } just Runs
        assertTrue(insertarProductosIniciales(bd))
        assertEquals(6, guardados.captured.size)
        assertEquals(6, guardados.captured.map { it.id }.distinct().size)
        assertTrue(guardados.captured.all { it.nombre.isNotBlank() && it.precio > 0 && it.stock >= 0 })
        coEvery { dao.contarProductos() } returns 6
        assertFalse(insertarProductosIniciales(bd))
        coVerify(exactly = 1) { dao.guardarProductos(any()) }
    }
}
