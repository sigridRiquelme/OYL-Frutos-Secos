package com.example.oylfrutossecos.data.repository

import com.example.oylfrutossecos.data.local.dao.ClienteDao
import com.example.oylfrutossecos.data.local.entity.ClienteEntity
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

class ClienteRepositoryTest {
    private val dao = mockk<ClienteDao>()
    private val repository = ClienteRepository(dao)
    private val cliente = ClienteEntity(1, "Cliente", "cliente", "cliente@example.test", "912345678", "Santiago", "hash-prueba", "salt-prueba")
    @Test fun delegaRegistroYConsultasSinAlterarDatos() = runTest {
        coEvery { dao.registrarCliente(cliente) } returns 1L
        coEvery { dao.buscarPorNombreUsuario("cliente") } returns cliente
        coEvery { dao.buscarPorId(1) } returns cliente
        coEvery { dao.buscarPorId(2) } returns null
        assertEquals(1L, repository.registrarCliente(cliente))
        assertSame(cliente, repository.buscarPorNombreUsuario("cliente"))
        assertSame(cliente, repository.buscarPorId(1)); assertNull(repository.buscarPorId(2))
        coVerify(exactly = 1) { dao.registrarCliente(cliente) }
    }
    @Test fun interpretaExistenciaYNumeroDeFilasActualizadas() = runTest {
        for (filas in listOf(0, 1, 2)) {
            coEvery { dao.existeNombreUsuario("cliente") } returns filas
            coEvery { dao.actualizarDatosCliente(1, "Cliente", "cliente", "cliente@example.test", "912345678", "Santiago") } returns filas
            assertEquals(filas > 0, repository.existeNombreUsuario("cliente"))
            assertEquals(filas == 1, repository.actualizarDatosCliente(1, "Cliente", "cliente", "cliente@example.test", "912345678", "Santiago"))
        }
    }
    @Test fun conservaErroresDePersistencia() {
        coEvery { dao.registrarCliente(any()) } throws IllegalStateException("BD no disponible")
        assertThrows(IllegalStateException::class.java) { kotlinx.coroutines.runBlocking { repository.registrarCliente(cliente) } }
    }
}
