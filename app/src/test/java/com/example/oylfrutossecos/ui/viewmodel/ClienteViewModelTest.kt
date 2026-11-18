
package com.example.oylfrutossecos.ui.viewmodel

import com.example.oylfrutossecos.data.local.entity.ClienteEntity
import com.example.oylfrutossecos.data.repository.ClienteRepository

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClienteViewModelTest {

    private val repository = mockk<ClienteRepository>()

    private val cliente = ClienteEntity(
        id = 7,
        nombre = "Ana Gomez",
        nombreUsuario = "ana",
        correo = "ana@correo.cl",
        telefono = "912345678",
        direccion = "Calle 123",
        passwordHash = "hash-de-prueba",
        passwordSalt = "salt-de-prueba"
    )

    @Before
    fun prepararCorrutinas() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun restaurarCorrutinas() {
        Dispatchers.resetMain()
    }

    private fun crearViewModel(
        dispatcher: CoroutineDispatcher
    ): ClienteViewModel {

        return ClienteViewModel(
            repository = repository,

            // Simulación únicamente para las pruebas.
            validarCorreoPerfil = { correo ->
                correo == "ana@correo.cl" ||
                        correo == "nuevo@correo.cl"
            },

            dispatcherPerfil = dispatcher
        )
    }

    // PRUEBA 1: Cargar cliente
    @Test
    fun cargarClienteCorrectamente() = runTest {

        coEvery {
            repository.buscarPorId(7)
        } returns cliente

        val viewModel = crearViewModel(
            StandardTestDispatcher(testScheduler)
        )

        viewModel.cargarCliente(7)
        advanceUntilIdle()

        viewModel.clienteActual.value shouldBe cliente
    }

    // PRUEBA 2: No editar sin un cliente cargado
    @Test
    fun noEditarSinCliente() = runTest {

        val viewModel = crearViewModel(
            StandardTestDispatcher(testScheduler)
        )

        viewModel.actualizarPerfil(
            nombre = "Ana Gomez",
            nombreUsuario = "ana",
            correo = "ana@correo.cl",
            telefono = "912345678",
            direccion = "Calle 123"
        )

        advanceUntilIdle()

        viewModel.estadoEdicionPerfil.value shouldBe
                EstadoEdicionPerfil.Error(
                    "No se encontro el cliente"
                )
    }

    // PRUEBA 3: Editar perfil correctamente
    @Test
    fun editarPerfilCorrectamente() = runTest {

        val actualizado = cliente.copy(
            nombre = "Ana Nueva",
            direccion = "Av Nueva 123"
        )

        coEvery {
            repository.buscarPorId(7)
        } returnsMany listOf(cliente, actualizado)

        coEvery {
            repository.actualizarDatosCliente(
                7,
                "Ana Nueva",
                "ana",
                "ana@correo.cl",
                "912345678",
                "Av Nueva 123"
            )
        } returns true

        val viewModel = crearViewModel(
            StandardTestDispatcher(testScheduler)
        )

        viewModel.cargarCliente(7)
        advanceUntilIdle()

        viewModel.actualizarPerfil(
            nombre = " Ana Nueva ",
            nombreUsuario = "ana",
            correo = "ana@correo.cl",
            telefono = "912345678",
            direccion = " Av Nueva 123 "
        )

        advanceUntilIdle()

        viewModel.estadoEdicionPerfil.value shouldBe
                EstadoEdicionPerfil.Exito

        viewModel.clienteActual.value shouldBe actualizado

        // No consulta duplicados si conserva el usuario.
        coVerify(exactly = 0) {
            repository.existeNombreUsuario(any())
        }
    }

    // PRUEBA 4: Rechazar nombre inválido
    @Test
    fun noEditarConNombreInvalido() = runTest {

        coEvery {
            repository.buscarPorId(7)
        } returns cliente

        val viewModel = crearViewModel(
            StandardTestDispatcher(testScheduler)
        )

        viewModel.cargarCliente(7)
        advanceUntilIdle()

        viewModel.actualizarPerfil(
            nombre = "Ab",
            nombreUsuario = "ana",
            correo = "ana@correo.cl",
            telefono = "912345678",
            direccion = "Calle 123"
        )

        advanceUntilIdle()

        viewModel.estadoEdicionPerfil.value shouldBe
                EstadoEdicionPerfil.Error(
                    "Revisa el nombre, usuario, correo y telefono"
                )

        coVerify(exactly = 0) {
            repository.actualizarDatosCliente(
                any(), any(), any(),
                any(), any(), any()
            )
        }
    }

    // PRUEBA 5: Rechazar correo incorrecto
    @Test
    fun noEditarConCorreoInvalido() = runTest {

        coEvery {
            repository.buscarPorId(7)
        } returns cliente

        val viewModel = crearViewModel(
            StandardTestDispatcher(testScheduler)
        )

        viewModel.cargarCliente(7)
        advanceUntilIdle()

        viewModel.actualizarPerfil(
            nombre = "Ana Gomez",
            nombreUsuario = "ana",
            correo = "correo-invalido",
            telefono = "912345678",
            direccion = "Calle 123"
        )

        advanceUntilIdle()

        viewModel.estadoEdicionPerfil.value shouldBe
                EstadoEdicionPerfil.Error(
                    "Revisa el nombre, usuario, correo y telefono"
                )
    }

    // PRUEBA 6: Rechazar usuario duplicado
    @Test
    fun noEditarConUsuarioDuplicado() = runTest {

        coEvery {
            repository.buscarPorId(7)
        } returns cliente

        coEvery {
            repository.existeNombreUsuario("otro")
        } returns true

        val viewModel = crearViewModel(
            StandardTestDispatcher(testScheduler)
        )

        viewModel.cargarCliente(7)
        advanceUntilIdle()

        viewModel.actualizarPerfil(
            nombre = "Ana Gomez",
            nombreUsuario = "otro",
            correo = "ana@correo.cl",
            telefono = "912345678",
            direccion = "Calle 123"
        )

        advanceUntilIdle()

        viewModel.estadoEdicionPerfil.value shouldBe
                EstadoEdicionPerfil.Error(
                    "El nombre de usuario ya existe"
                )

        coVerify(exactly = 0) {
            repository.actualizarDatosCliente(
                any(), any(), any(),
                any(), any(), any()
            )
        }
    }

    // PRUEBA 7: El repositorio rechaza la actualización
    @Test
    fun errorAlGuardarPerfil() = runTest {

        coEvery {
            repository.buscarPorId(7)
        } returns cliente

        coEvery {
            repository.actualizarDatosCliente(
                7,
                "Ana Gomez",
                "ana",
                "ana@correo.cl",
                "912345678",
                "Calle 123"
            )
        } returns false

        val viewModel = crearViewModel(
            StandardTestDispatcher(testScheduler)
        )

        viewModel.cargarCliente(7)
        advanceUntilIdle()

        viewModel.actualizarPerfil(
            "Ana Gomez",
            "ana",
            "ana@correo.cl",
            "912345678",
            "Calle 123"
        )

        advanceUntilIdle()

        viewModel.estadoEdicionPerfil.value shouldBe
                EstadoEdicionPerfil.Error(
                    "No se pudieron guardar los cambios"
                )
    }

    // PRUEBA 8: Manejar excepción del repositorio
    @Test
    fun excepcionAlActualizarPerfil() = runTest {

        coEvery {
            repository.buscarPorId(7)
        } returns cliente

        coEvery {
            repository.actualizarDatosCliente(
                7,
                "Ana Gomez",
                "ana",
                "ana@correo.cl",
                "912345678",
                "Calle 123"
            )
        } throws IllegalStateException("Error de base de datos")

        val viewModel = crearViewModel(
            StandardTestDispatcher(testScheduler)
        )

        viewModel.cargarCliente(7)
        advanceUntilIdle()

        viewModel.actualizarPerfil(
            "Ana Gomez",
            "ana",
            "ana@correo.cl",
            "912345678",
            "Calle 123"
        )

        advanceUntilIdle()

        viewModel.estadoEdicionPerfil.value shouldBe
                EstadoEdicionPerfil.Error(
                    "No se pudo actualizar el perfil. " +
                            "Comprueba que el usuario no exista."
                )
    }

    // PRUEBA 9: Limpiar el perfil y su estado
    @Test
    fun limpiarPerfilYEstado() = runTest {

        coEvery {
            repository.buscarPorId(7)
        } returns cliente

        val viewModel = crearViewModel(
            StandardTestDispatcher(testScheduler)
        )

        viewModel.cargarCliente(7)
        advanceUntilIdle()

        viewModel.limpiarClienteActual()
        viewModel.limpiarEstadoEdicionPerfil()

        viewModel.clienteActual.value shouldBe null

        viewModel.estadoEdicionPerfil.value shouldBe
                EstadoEdicionPerfil.Inicial
    }
}
