
package com.example.oylfrutossecos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.oylfrutossecos.data.local.entity.ClienteEntity
import com.example.oylfrutossecos.data.repository.ClienteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import android.util.Base64

sealed interface EstadoRegistro {
    data object Inicial : EstadoRegistro
    data object Cargando : EstadoRegistro
    data object Exito : EstadoRegistro
    data class Error(val mensaje: String) : EstadoRegistro
}

// NUEVO: Estados para el inicio de sesión
sealed interface EstadoLogin {
    data object Inicial : EstadoLogin
    data object Cargando : EstadoLogin
    data class Exito(val clienteId: Int) : EstadoLogin
    data class Error(val mensaje: String) : EstadoLogin
}


sealed interface EstadoEdicionPerfil {
    data object Inicial : EstadoEdicionPerfil
    data object Guardando : EstadoEdicionPerfil
    data object Exito : EstadoEdicionPerfil
    data class Error(val mensaje: String) : EstadoEdicionPerfil
}

class ClienteViewModel(
    private val repository: ClienteRepository
) : ViewModel() {

    private val _estadoRegistro =
        MutableStateFlow<EstadoRegistro>(EstadoRegistro.Inicial)

    val estadoRegistro: StateFlow<EstadoRegistro> =
        _estadoRegistro

    // Estado del inicio de sesión
    private val _estadoLogin =
        MutableStateFlow<EstadoLogin>(EstadoLogin.Inicial)

    val estadoLogin: StateFlow<EstadoLogin> =
        _estadoLogin

    // Cliente que inició sesión
    private val _clienteActual =
        MutableStateFlow<ClienteEntity?>(null)

    val clienteActual: StateFlow<ClienteEntity?> =
        _clienteActual


    private val _estadoEdicionPerfil =
        MutableStateFlow<EstadoEdicionPerfil>(
            EstadoEdicionPerfil.Inicial
        )

    val estadoEdicionPerfil: StateFlow<EstadoEdicionPerfil> =
        _estadoEdicionPerfil

    fun registrarCliente(
        nombre: String,
        nombreUsuario: String,
        correo: String,
        telefono: String,
        direccion: String,
        contrasena: String
    ) {
        if (_estadoRegistro.value == EstadoRegistro.Cargando) return

        viewModelScope.launch {
            _estadoRegistro.value = EstadoRegistro.Cargando

            try {
                val usuario = nombreUsuario.trim()

                if (nombre.trim().length < 3 ||
                    usuario.length < 3 ||
                    !android.util.Patterns.EMAIL_ADDRESS
                        .matcher(correo.trim()).matches() ||
                    telefono.count { it.isDigit() } < 9 ||
                    contrasena.length < 6
                ) {
                    _estadoRegistro.value =
                        EstadoRegistro.Error("Revisa los datos del formulario")
                    return@launch
                }

                val existe = withContext(Dispatchers.IO) {
                    repository.existeNombreUsuario(usuario)
                }

                if (existe) {
                    _estadoRegistro.value =
                        EstadoRegistro.Error("El nombre de usuario ya existe")
                    return@launch
                }

                val salt = ByteArray(16).also {
                    SecureRandom().nextBytes(it)
                }

                val hash = withContext(Dispatchers.Default) {
                    val spec = PBEKeySpec(
                        contrasena.toCharArray(),
                        salt,
                        210_000,
                        256
                    )
                    try {
                        SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA256"
                        ).generateSecret(spec).encoded
                    } finally {
                        spec.clearPassword()
                    }
                }

                val cliente = ClienteEntity(
                    nombre = nombre.trim(),
                    nombreUsuario = usuario,
                    correo = correo.trim(),
                    telefono = telefono.trim(),
                    direccion = direccion.trim(),
                    passwordHash = Base64.encodeToString(
                        hash, Base64.NO_WRAP
                    ),
                    passwordSalt = Base64.encodeToString(
                        salt, Base64.NO_WRAP
                    )
                )

                withContext(Dispatchers.IO) {
                    repository.registrarCliente(cliente)
                }

                _estadoRegistro.value = EstadoRegistro.Exito

            } catch (e: Exception) {
                android.util.Log.e(
                    "OYL_REGISTRO",
                    "Error al registrar cliente",
                    e
                )

                _estadoRegistro.value = EstadoRegistro.Error(
                    "No se pudo registrar el cliente. Intenta nuevamente."
                )
            }
        }
    }

    fun limpiarEstado() {
        _estadoRegistro.value = EstadoRegistro.Inicial
    }


    fun iniciarSesion(
        nombreUsuario: String,
        contrasena: String
    ) {
        if (_estadoLogin.value == EstadoLogin.Cargando) return

        viewModelScope.launch {
            _estadoLogin.value = EstadoLogin.Cargando

            try {
                val cliente = withContext(Dispatchers.IO) {
                    repository.buscarPorNombreUsuario(
                        nombreUsuario.trim()
                    )
                }

                if (cliente == null) {
                    _estadoLogin.value = EstadoLogin.Error(
                        "Nombre de usuario o contraseña incorrectos"
                    )
                    return@launch
                }

                val contrasenaCorrecta =
                    withContext(Dispatchers.Default) {

                        val salt = Base64.decode(
                            cliente.passwordSalt,
                            Base64.NO_WRAP
                        )

                        val hashGuardado = Base64.decode(
                            cliente.passwordHash,
                            Base64.NO_WRAP
                        )

                        val spec = PBEKeySpec(
                            contrasena.toCharArray(),
                            salt,
                            210_000,
                            hashGuardado.size * 8
                        )

                        val hashIngresado = try {
                            SecretKeyFactory.getInstance(
                                "PBKDF2WithHmacSHA256"
                            ).generateSecret(spec).encoded
                        } finally {
                            spec.clearPassword()
                        }

                        java.security.MessageDigest.isEqual(
                            hashGuardado,
                            hashIngresado
                        )
                    }

                if (contrasenaCorrecta) {
                    _estadoLogin.value = EstadoLogin.Exito(cliente.id)
                } else {
                    _estadoLogin.value = EstadoLogin.Error(
                        "Nombre de usuario o contraseña incorrectos"
                    )
                }

            } catch (e: Exception) {
                _estadoLogin.value = EstadoLogin.Error(
                    "No se pudo iniciar sesión. Intenta nuevamente."
                )
            }
        }
    }

    fun limpiarEstadoLogin() {
        _estadoLogin.value = EstadoLogin.Inicial
    }


    fun cargarCliente(clienteId: Int) {
        viewModelScope.launch {
            try {
                val cliente = withContext(Dispatchers.IO) {
                    repository.buscarPorId(clienteId)
                }

                _clienteActual.value = cliente

            } catch (e: Exception) {
                _clienteActual.value = null
            }
        }
    }


    fun actualizarPerfil(
        nombre: String,
        correo: String,
        telefono: String,
        direccion: String
    ) {
        if (_estadoEdicionPerfil.value == EstadoEdicionPerfil.Guardando) {
            return
        }

        viewModelScope.launch {
            _estadoEdicionPerfil.value = EstadoEdicionPerfil.Guardando

            val cliente = _clienteActual.value

            if (cliente == null) {
                _estadoEdicionPerfil.value = EstadoEdicionPerfil.Error(
                    "No se encontró el cliente"
                )
                return@launch
            }

            if (nombre.trim().length < 3 ||
                !android.util.Patterns.EMAIL_ADDRESS
                    .matcher(correo.trim()).matches() ||
                telefono.count { it.isDigit() } < 9
            ) {
                _estadoEdicionPerfil.value = EstadoEdicionPerfil.Error(
                    "Revisa el nombre, correo y teléfono"
                )
                return@launch
            }

            try {
                val actualizado = withContext(Dispatchers.IO) {
                    repository.actualizarDatosCliente(
                        clienteId = cliente.id,
                        nombre = nombre.trim(),
                        correo = correo.trim(),
                        telefono = telefono.trim(),
                        direccion = direccion.trim()
                    )
                }

                if (actualizado) {
                    // Recuperar desde Room los datos actualizados
                    cargarCliente(cliente.id)

                    _estadoEdicionPerfil.value = EstadoEdicionPerfil.Exito
                } else {
                    _estadoEdicionPerfil.value = EstadoEdicionPerfil.Error(
                        "No se pudieron guardar los cambios"
                    )
                }

            } catch (e: Exception) {
                _estadoEdicionPerfil.value = EstadoEdicionPerfil.Error(
                    "Ocurrió un error al actualizar el perfil"
                )
            }
        }
    }

    fun limpiarEstadoEdicionPerfil() {
        _estadoEdicionPerfil.value = EstadoEdicionPerfil.Inicial
    }



}

class ClienteViewModelFactory(
    private val repository: ClienteRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(ClienteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClienteViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}
