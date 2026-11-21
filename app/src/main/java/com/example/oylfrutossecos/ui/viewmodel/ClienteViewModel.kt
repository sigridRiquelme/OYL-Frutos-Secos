
package com.example.oylfrutossecos.ui.viewmodel

import android.util.Log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

import com.example.oylfrutossecos.data.local.entity.ClienteEntity
import com.example.oylfrutossecos.data.repository.ClienteRepository

import com.example.oylfrutossecos.feature.autenticacion.domain.ValidadorRegistro
import com.example.oylfrutossecos.feature.autenticacion.platform.ValidadorCorreoAndroid
import com.example.oylfrutossecos.feature.perfil.domain.ValidadorPerfil

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import com.example.oylfrutossecos.feature.autenticacion.domain.SeguridadContrasena

// ============================================================
// ESTADOS DEL REGISTRO
// ============================================================

sealed interface EstadoRegistro {

    data object Inicial : EstadoRegistro

    data object Cargando : EstadoRegistro

    data object Exito : EstadoRegistro

    data class Error(
        val mensaje: String
    ) : EstadoRegistro
}


// ============================================================
// ESTADOS DEL INICIO DE SESIÓN
// ============================================================

sealed interface EstadoLogin {

    data object Inicial : EstadoLogin

    data object Cargando : EstadoLogin

    data class Exito(
        val clienteId: Int
    ) : EstadoLogin

    data class Error(
        val mensaje: String
    ) : EstadoLogin
}


// ============================================================
// ESTADOS DE EDICIÓN DEL PERFIL
// ============================================================

sealed interface EstadoEdicionPerfil {

    data object Inicial : EstadoEdicionPerfil

    data object Guardando : EstadoEdicionPerfil

    data object Exito : EstadoEdicionPerfil

    data class Error(
        val mensaje: String
    ) : EstadoEdicionPerfil
}


// ============================================================
// VIEWMODEL DE CLIENTES
// ============================================================


class ClienteViewModel(
    private val repository: ClienteRepository,

    // Validación del correo en la edición del perfil
    private val validarCorreoPerfil: (String) -> Boolean = {
        ValidadorCorreoAndroid.esValido(it)
    },

    // Dispatcher para operaciones del perfil
    private val dispatcherPerfil: CoroutineDispatcher =
        Dispatchers.IO,

    // Validación del correo durante el registro
    private val validarCorreoRegistro: (String) -> Boolean = {
        ValidadorCorreoAndroid.esValido(it)
    },

    // Dispatcher para operaciones de autenticación
    private val dispatcherAutenticacion: CoroutineDispatcher =
        Dispatchers.IO,

    // Dispatcher para generar y verificar contraseñas
    private val dispatcherSeguridad: CoroutineDispatcher =
        Dispatchers.Default

) : ViewModel() {



    // --------------------------------------------------------
    // ESTADO DEL REGISTRO
    // --------------------------------------------------------

    private val _estadoRegistro =
        MutableStateFlow<EstadoRegistro>(
            EstadoRegistro.Inicial
        )

    val estadoRegistro: StateFlow<EstadoRegistro> =
        _estadoRegistro


    // --------------------------------------------------------
    // ESTADO DEL INICIO DE SESIÓN
    // --------------------------------------------------------

    private val _estadoLogin =
        MutableStateFlow<EstadoLogin>(
            EstadoLogin.Inicial
        )

    val estadoLogin: StateFlow<EstadoLogin> =
        _estadoLogin


    // --------------------------------------------------------
    // CLIENTE ACTUAL
    // --------------------------------------------------------

    private val _clienteActual =
        MutableStateFlow<ClienteEntity?>(null)

    val clienteActual: StateFlow<ClienteEntity?> =
        _clienteActual


    // --------------------------------------------------------
    // ESTADO DE EDICIÓN DEL PERFIL
    // --------------------------------------------------------

    private val _estadoEdicionPerfil =
        MutableStateFlow<EstadoEdicionPerfil>(
            EstadoEdicionPerfil.Inicial
        )

    val estadoEdicionPerfil: StateFlow<EstadoEdicionPerfil> =
        _estadoEdicionPerfil


    // ========================================================
    // REGISTRAR CLIENTE
    // ========================================================


    fun registrarCliente(
        nombre: String,
        nombreUsuario: String,
        correo: String,
        telefono: String,
        direccion: String,
        contrasena: String
    ) {

        if (_estadoRegistro.value == EstadoRegistro.Cargando) {
            return
        }

        viewModelScope.launch {

            _estadoRegistro.value = EstadoRegistro.Cargando

            try {

                val usuario = nombreUsuario.trim()

                // Validaciones centralizadas
                val registroValido =
                    ValidadorRegistro.esRegistroValido(
                        nombre = nombre,
                        usuario = usuario,correoValido = validarCorreoRegistro(correo.trim()),
                        telefono = telefono,
                        contrasena = contrasena
                    )

                if (!registroValido) {

                    _estadoRegistro.value =
                        EstadoRegistro.Error(
                            "Revisa los datos del formulario"
                        )

                    return@launch
                }

                // Comprobar nombre de usuario duplicado
                val existe = withContext(dispatcherAutenticacion) {
                    repository.existeNombreUsuario(usuario)
                }

                if (existe) {

                    _estadoRegistro.value =
                        EstadoRegistro.Error(
                            "El nombre de usuario ya existe"
                        )

                    return@launch
                }

                // Generar credenciales mediante
                // nuestro módulo de seguridad
                val credenciales =
                    withContext(dispatcherSeguridad) {
                        SeguridadContrasena.generarHash(
                            contrasena
                        )
                    }

                // Preparar cliente para guardarlo en Room
                val cliente = ClienteEntity(
                    nombre = nombre.trim(),
                    nombreUsuario = usuario,
                    correo = correo.trim(),
                    telefono = telefono.trim(),
                    direccion = direccion.trim(),
                    passwordHash = credenciales.hash,
                    passwordSalt = credenciales.salt
                )

                withContext(dispatcherAutenticacion) {
                    repository.registrarCliente(cliente)
                }

                _estadoRegistro.value = EstadoRegistro.Exito

            } catch (e: Exception) {

                Log.e(
                    "OYL_REGISTRO",
                    "Error al registrar cliente",
                    e
                )

                _estadoRegistro.value =
                    EstadoRegistro.Error(
                        "No se pudo registrar el cliente. " +
                                "Intenta nuevamente."
                    )
            }
        }
    }


    // Limpiar estado del registro
    fun limpiarEstado() {

        _estadoRegistro.value = EstadoRegistro.Inicial
    }


    // ========================================================
    // INICIAR SESIÓN
    // ========================================================


    fun iniciarSesion(
        nombreUsuario: String,
        contrasena: String
    ) {

        if (_estadoLogin.value == EstadoLogin.Cargando) {
            return
        }

        viewModelScope.launch {

            _estadoLogin.value = EstadoLogin.Cargando

            try {

                // Buscar cliente en la base de datos
                val cliente = withContext(dispatcherSeguridad) {
                    repository.buscarPorNombreUsuario(
                        nombreUsuario.trim()
                    )
                }

                if (cliente == null) {

                    _estadoLogin.value =
                        EstadoLogin.Error(
                            "Nombre de usuario o contraseña incorrectos"
                        )

                    return@launch
                }

                // Verificar mediante nuestro módulo
                // de seguridad independiente
                val correcto =
                    withContext(dispatcherSeguridad) {

                        SeguridadContrasena.verificarContrasena(
                            contrasena = contrasena,
                            hashGuardado = cliente.passwordHash,
                            saltGuardado = cliente.passwordSalt
                        )
                    }

                _estadoLogin.value = if (correcto) {

                    EstadoLogin.Exito(cliente.id)

                } else {

                    EstadoLogin.Error(
                        "Nombre de usuario o contraseña incorrectos"
                    )
                }

            } catch (e: Exception) {

                _estadoLogin.value =
                    EstadoLogin.Error(
                        "No se pudo iniciar sesion. " +
                                "Intenta nuevamente."
                    )
            }
        }
    }


    // Limpiar estado del inicio de sesión
    fun limpiarEstadoLogin() {

        _estadoLogin.value = EstadoLogin.Inicial
    }


    // ========================================================
    // LIMPIAR CLIENTE ACTUAL
    // ========================================================

    fun limpiarClienteActual() {

        _clienteActual.value = null
    }


    // ========================================================
    // CARGAR CLIENTE
    // ========================================================

    fun cargarCliente(clienteId: Int) {

        viewModelScope.launch {

            _clienteActual.value = try {

                // Dispatcher configurable para las pruebas
                withContext(dispatcherPerfil) {

                    repository.buscarPorId(clienteId)
                }

            } catch (_: Exception) {

                null
            }
        }
    }


    // ========================================================
    // ACTUALIZAR PERFIL
    // ========================================================

    fun actualizarPerfil(
        nombre: String,
        nombreUsuario: String,
        correo: String,
        telefono: String,
        direccion: String
    ) {

        if (
            _estadoEdicionPerfil.value ==
            EstadoEdicionPerfil.Guardando
        ) {
            return
        }

        viewModelScope.launch {

            _estadoEdicionPerfil.value =
                EstadoEdicionPerfil.Guardando


            // Comprobar cliente autenticado
            val cliente = _clienteActual.value

            if (cliente == null) {

                _estadoEdicionPerfil.value =
                    EstadoEdicionPerfil.Error(
                        "No se encontro el cliente"
                    )

                return@launch
            }


            val alias = nombreUsuario.trim()


            // Validaciones centralizadas del perfil
            val perfilValido =
                ValidadorPerfil.esPerfilValido(
                    nombre = nombre,
                    nombreUsuario = alias,

                    // Validación configurable para tests
                    correoValido = validarCorreoPerfil(
                        correo.trim()
                    ),

                    telefono = telefono
                )


            if (!perfilValido) {

                _estadoEdicionPerfil.value =
                    EstadoEdicionPerfil.Error(
                        "Revisa el nombre, usuario, correo y telefono"
                    )

                return@launch
            }


            try {

                // Verificar si cambió el nombre de usuario
                if (
                    !alias.equals(
                        cliente.nombreUsuario,
                        ignoreCase = true
                    )
                ) {

                    val existe =
                        withContext(dispatcherPerfil) {

                            repository.existeNombreUsuario(
                                alias
                            )
                        }


                    // No permitir usuarios duplicados
                    if (existe) {

                        _estadoEdicionPerfil.value =
                            EstadoEdicionPerfil.Error(
                                "El nombre de usuario ya existe"
                            )

                        return@launch
                    }
                }


                // Guardar cambios en Room
                val actualizado =
                    withContext(dispatcherPerfil) {

                        repository.actualizarDatosCliente(
                            clienteId = cliente.id,
                            nombre = nombre.trim(),
                            nombreUsuario = alias,
                            correo = correo.trim(),
                            telefono = telefono.trim(),
                            direccion = direccion.trim()
                        )
                    }


                if (actualizado) {

                    // Recuperar el cliente actualizado
                    // antes de anunciar el éxito.
                    _clienteActual.value =
                        withContext(dispatcherPerfil) {

                            repository.buscarPorId(
                                cliente.id
                            )
                        }


                    _estadoEdicionPerfil.value =
                        EstadoEdicionPerfil.Exito

                } else {

                    _estadoEdicionPerfil.value =
                        EstadoEdicionPerfil.Error(
                            "No se pudieron guardar los cambios"
                        )
                }

            } catch (e: Exception) {

                _estadoEdicionPerfil.value =
                    EstadoEdicionPerfil.Error(
                        "No se pudo actualizar el perfil. " +
                                "Comprueba que el usuario no exista."
                    )
            }
        }
    }


    // ========================================================
    // LIMPIAR ESTADO DE EDICIÓN
    // ========================================================

    fun limpiarEstadoEdicionPerfil() {

        _estadoEdicionPerfil.value =
            EstadoEdicionPerfil.Inicial
    }
}


// ============================================================
// FACTORY DEL VIEWMODEL
// ============================================================

class ClienteViewModelFactory(
    private val repository: ClienteRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (
            modelClass.isAssignableFrom(
                ClienteViewModel::class.java
            )
        ) {

            @Suppress("UNCHECKED_CAST")
            return ClienteViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido"
        )
    }
}
