
package com.example.oylfrutossecos.ui.viewmodel

import com.example.oylfrutossecos.data.local.entity.ClienteEntity
import com.example.oylfrutossecos.data.repository.ClienteRepository
import com.example.oylfrutossecos.feature.autenticacion.domain.SeguridadContrasena

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

import kotlinx.coroutines.test.TestDispatcher
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
class ClienteAutenticacionViewModelTest {

    private val repository = mockk<ClienteRepository>()

    @Before
    fun prepararCorrutinas() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun restaurarCorrutinas() {
        Dispatchers.resetMain()
    }

    // ViewModel configurado para pruebas

    private fun crearViewModel(
        dispatcher: TestDispatcher
    ): ClienteViewModel {

        return ClienteViewModel(
            repository = repository,

            validarCorreoPerfil = { true },
            dispatcherPerfil = dispatcher,

            validarCorreoRegistro = { correo ->
                correo == "ana@correo.cl"
            },

            dispatcherAutenticacion = dispatcher,
            dispatcherSeguridad = dispatcher
        )
    }


    // Cliente con una contraseña protegida real
    private fun clienteDePrueba(): ClienteEntity {

        val credenciales =
            SeguridadContrasena.generarHash("Clave123!")

        return ClienteEntity(
            id = 7,
            nombre = "Ana Gomez",
            nombreUsuario = "ana",
            correo = "ana@correo.cl",
            telefono = "912345678",
            direccion = "Calle 123",
            passwordHash = credenciales.hash,
            passwordSalt = credenciales.salt
        )
    }

    // PRUEBA 1: Registro correcto
    @Test
    fun registrarClienteCorrectamente() = runTest {

        coEvery {
            repository.existeNombreUsuario("ana")
        } returns false

        coEvery {
            repository.registrarCliente(any())
        } returns 7L

        val viewModel = crearViewModel(
            StandardTestDispatcher(testScheduler)
        )

        viewModel.registrarCliente(
            nombre = " Ana Gomez ",
            nombreUsuario = " ana ",
            correo = "ana@correo.cl",
            telefono = "912345678",
            direccion = "Calle 123",
            contrasena = "Clave123!"
        )

        advanceUntilIdle()

        viewModel.estadoRegistro.value shouldBe
                EstadoRegistro.Exito

        coVerify(exactly = 1) {
            repository.registrarCliente(
                match {
                    it.nombre == "Ana Gomez" &&
                            it.nombreUsuario == "ana" &&
                            it.passwordHash.isNotBlank() &&
                            it.passwordSalt.isNotBlank()
                }
            )
        }
    }

    // PRUEBA 2: Nombre inválido
    @Test
    fun rechazarRegistroConNombreCorto() = runTest {

        val viewModel = crearViewModel(
            StandardTestDispatcher(testScheduler)
        )

        viewModel.registrarCliente(
            "Ab",
            "ana",
            "ana@correo.cl",
            "912345678",
            "Calle 123",
            "Clave123!"
        )

        advanceUntilIdle()

        viewModel.estadoRegistro.value shouldBe
                EstadoRegistro.Error(
                    "Revisa los datos del formulario"
                )

        coVerify(exactly = 0) {
            repository.registrarCliente(any())
        }
    }

    // PRUEBA 3: Correo inválido
    @Test
    fun rechazarRegistroConCorreoInvalido() = runTest {

        val viewModel = crearViewModel(
            StandardTestDispatcher(testScheduler)
        )

        viewModel.registrarCliente(
            "Ana Gomez",
            "ana",
            "correo-invalido",
            "912345678",
            "Calle 123",
            "Clave123!"
        )

        advanceUntilIdle()

        viewModel.estadoRegistro.value shouldBe
                EstadoRegistro.Error(
                    "Revisa los datos del formulario"
                )
    }

    // PRUEBA 4: Usuario duplicado
    @Test
    fun rechazarRegistroDuplicado() = runTest {

        coEvery {
            repository.existeNombreUsuario("ana")
        } returns true

        val viewModel = crearViewModel(
            StandardTestDispatcher(testScheduler)
        )

        viewModel.registrarCliente(
            "Ana Gomez",
            "ana",
            "ana@correo.cl",
            "912345678",
            "Calle 123",
            "Clave123!"
        )

        advanceUntilIdle()

        viewModel.estadoRegistro.value shouldBe
                EstadoRegistro.Error(
                    "El nombre de usuario ya existe"
                )

        coVerify(exactly = 0) {
            repository.registrarCliente(any())
        }
    }

    // PRUEBA 5: Inicio de sesión correcto
    @Test
    fun iniciarSesionCorrectamente() = runTest {

        coEvery {
            repository.buscarPorNombreUsuario("ana")
        } returns clienteDePrueba()

        val viewModel = crearViewModel(
            StandardTestDispatcher(testScheduler)
        )

        viewModel.iniciarSesion(
            nombreUsuario = "ana",
            contrasena = "Clave123!"
        )

        advanceUntilIdle()

        viewModel.estadoLogin.value shouldBe
                EstadoLogin.Exito(7)
    }

    // PRUEBA 6: Contraseña incorrecta
    @Test
    fun rechazarContrasenaIncorrecta() = runTest {

        coEvery {
            repository.buscarPorNombreUsuario("ana")
        } returns clienteDePrueba()

        val viewModel = crearViewModel(
            StandardTestDispatcher(testScheduler)
        )

        viewModel.iniciarSesion(
            nombreUsuario = "ana",
            contrasena = "ClaveEquivocada"
        )

        advanceUntilIdle()

        viewModel.estadoLogin.value shouldBe
                EstadoLogin.Error(
                    "Nombre de usuario o contraseña incorrectos"
                )
    }

    // PRUEBA 7: Usuario inexistente
    @Test
    fun rechazarUsuarioInexistente() = runTest {

        coEvery {
            repository.buscarPorNombreUsuario("desconocido")
        } returns null

        val viewModel = crearViewModel(
            StandardTestDispatcher(testScheduler)
        )

        viewModel.iniciarSesion(
            nombreUsuario = "desconocido",
            contrasena = "Clave123!"
        )

        advanceUntilIdle()

        viewModel.estadoLogin.value shouldBe
                EstadoLogin.Error(
                    "Nombre de usuario o contraseña incorrectos"
                )
    }

    // PRUEBA 8: Error del repositorio
    @Test
    fun manejarErrorDeInicioSesion() = runTest {

        coEvery {
            repository.buscarPorNombreUsuario("ana")
        } throws IllegalStateException(
            "Error de base de datos"
        )

        val viewModel = crearViewModel(
            StandardTestDispatcher(testScheduler)
        )

        viewModel.iniciarSesion(
            "ana",
            "Clave123!"
        )

        advanceUntilIdle()

        viewModel.estadoLogin.value shouldBe
                EstadoLogin.Error(
                    "No se pudo iniciar sesion. Intenta nuevamente."
                )
    }
}
