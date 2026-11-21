
package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import com.example.oylfrutossecos.ui.viewmodel.ClienteViewModel
import com.example.oylfrutossecos.ui.viewmodel.EstadoRegistro
import com.example.oylfrutossecos.feature.autenticacion.domain.ValidadorRegistro
import com.example.oylfrutossecos.feature.autenticacion.platform.ValidadorCorreoAndroid
import com.example.oylfrutossecos.data.repository.RegistroRemoto
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import retrofit2.HttpException

private val usuarioBackend = Regex("[a-zA-Z0-9._-]{3,80}")
private val telefonoBackend = Regex("[+0-9 ()-]{1,30}")

@Composable
fun RegistroScreen(
    clienteViewModel: ClienteViewModel,
    modifier: Modifier = Modifier,
    onVolverLogin: () -> Unit = {},
    registrarEnBackend: (suspend (RegistroRemoto) -> Unit)? = null
) {

    var nombre by remember { mutableStateOf("") }
    var nombreUsuario by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }

    var mostrarErrores by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }
    var enviando by remember { mutableStateOf(false) }
    val alcance = rememberCoroutineScope()
    val remoto = registrarEnBackend != null

    val estadoRegistro by clienteViewModel.estadoRegistro.collectAsState()

    LaunchedEffect(estadoRegistro, remoto) {
        if (!remoto && estadoRegistro == EstadoRegistro.Exito) {
            clienteViewModel.limpiarEstado()
            onVolverLogin()
        }
    }


    // Validaciones centralizadas
    val nombreInvalido =
        !ValidadorRegistro.esNombreValido(nombre) || (remoto && nombre.trim().length > 120)

    val nombreUsuarioInvalido =
        !ValidadorRegistro.esUsuarioValido(nombreUsuario) ||
                (remoto && !usuarioBackend.matches(nombreUsuario.trim()))

    val correoInvalido =
        !ValidadorCorreoAndroid.esValido(correo.trim()) || (remoto && correo.trim().length > 150)

    val telefonoInvalido =
        !ValidadorRegistro.esTelefonoValido(telefono) ||
                (remoto && !telefonoBackend.matches(telefono.trim()))

    val contrasenaInvalida =
        if (remoto) contrasena.isBlank() || contrasena.length < 8 ||
                contrasena.toByteArray(Charsets.UTF_8).size > 72
        else !ValidadorRegistro.esContrasenaValida(contrasena)
    val direccionInvalida = remoto && direccion.trim().length > 250


    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Crear Cuenta",
            fontSize = 28.sp
        )

        Text(
            text = "Regístrate en OYL Frutos Secos"
        )

        OutlinedTextField(
            value = nombre,
            enabled = !enviando,
            onValueChange = { nombre = it },
            label = { Text("Nombre completo") },
            modifier = Modifier.fillMaxWidth(),
            isError = mostrarErrores && nombreInvalido,
            supportingText = {
                if (mostrarErrores && nombreInvalido)
                    Text(if (remoto) "Usa entre 3 y 120 caracteres" else "Ingresa al menos 3 caracteres")
            },
            singleLine = true
        )


        OutlinedTextField(
            value = nombreUsuario,
            enabled = !enviando,
            onValueChange = {
                nombreUsuario = it

                // Limpiar el error al comenzar a corregirlo
                if (estadoRegistro is EstadoRegistro.Error) {
                    clienteViewModel.limpiarEstado()
                }
            },
            label = { Text("Nombre de usuario") },
            modifier = Modifier.fillMaxWidth(),

            isError = (mostrarErrores && nombreUsuarioInvalido) ||
                    (estadoRegistro is EstadoRegistro.Error &&
                            (estadoRegistro as EstadoRegistro.Error).mensaje ==
                            "El nombre de usuario ya existe"),

            supportingText = {
                if (mostrarErrores && nombreUsuarioInvalido) {
                    Text(if (remoto) "Usa de 3 a 80 letras, números, puntos, guiones o guiones bajos" else "Ingresa al menos 3 caracteres")
                } else if (
                    estadoRegistro is EstadoRegistro.Error &&
                    (estadoRegistro as EstadoRegistro.Error).mensaje ==
                    "El nombre de usuario ya existe"
                ) {
                    Text("El nombre de usuario ya existe")
                }
            },

            singleLine = true
        )


        OutlinedTextField(
            value = correo,
            enabled = !enviando,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            isError = mostrarErrores && correoInvalido,
            supportingText = {
                if (mostrarErrores && correoInvalido)
                    Text("Ingresa un correo válido")
            },
            singleLine = true
        )

        OutlinedTextField(
            value = telefono,
            enabled = !enviando,
            onValueChange = { telefono = it },
            label = { Text("Teléfono") },
            modifier = Modifier.fillMaxWidth(),
            isError = mostrarErrores && telefonoInvalido,
            supportingText = {
                if (mostrarErrores && telefonoInvalido)
                    Text("Ingresa un teléfono válido")
            },
            singleLine = true
        )

        OutlinedTextField(
            value = contrasena,
            enabled = !enviando,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            isError = mostrarErrores && contrasenaInvalida,
            supportingText = {
                if (mostrarErrores && contrasenaInvalida)
                    Text(if (remoto) "Mínimo 8 caracteres y máximo 72 bytes" else "Mínimo 6 caracteres")
            },
            singleLine = true
        )

        OutlinedTextField(
            value = direccion,
            enabled = !enviando,
            onValueChange = { direccion = it },
            label = { Text("Dirección (opcional)") },
            isError = mostrarErrores && direccionInvalida,
            supportingText = {
                if (mostrarErrores && direccionInvalida) Text("Máximo 250 caracteres")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                mostrarErrores = true

                if (
                    !nombreInvalido &&
                    !nombreUsuarioInvalido &&
                    !correoInvalido &&
                    !telefonoInvalido &&
                    !contrasenaInvalida &&
                    !direccionInvalida
                ) {
                    mensaje = ""

                    if (registrarEnBackend != null && !enviando) {
                        val datos = RegistroRemoto(
                            nombre.trim(), nombreUsuario.trim(), correo.trim(), telefono.trim(),
                            direccion.trim().ifEmpty { null }, contrasena
                        )
                        enviando = true
                        alcance.launch {
                            try {
                                registrarEnBackend(datos)
                                contrasena = ""
                                onVolverLogin()
                            } catch (cancelada: CancellationException) {
                                throw cancelada
                            } catch (error: HttpException) {
                                mensaje = when (error.code()) {
                                    400 -> "Revisa los datos ingresados."
                                    409 -> "El nombre de usuario o el correo ya está registrado."
                                    else -> "No pudimos crear la cuenta. Intenta nuevamente."
                                }
                            } catch (_: IOException) {
                                mensaje = "No pudimos confirmar el registro. Si ya creaste la cuenta, prueba iniciar sesión."
                            } finally {
                                enviando = false
                            }
                        }
                    } else if (!remoto) clienteViewModel.registrarCliente(
                        nombre = nombre,
                        nombreUsuario = nombreUsuario,
                        correo = correo,
                        telefono = telefono,
                        direccion = direccion,
                        contrasena = contrasena
                    )
                } else {
                    mensaje = "Corrige los campos marcados."
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !enviando && (remoto || estadoRegistro != EstadoRegistro.Cargando)
        ) {
            Text(
                if (enviando || (!remoto && estadoRegistro == EstadoRegistro.Cargando))
                    "Guardando..."
                else
                    "Crear Cuenta"
            )
        }

        if (mensaje.isNotEmpty()) {
            Text(text = mensaje, color = MaterialTheme.colorScheme.error)
        }

        if (estadoRegistro is EstadoRegistro.Error) {
            val error = (estadoRegistro as EstadoRegistro.Error).mensaje

            if (error != "El nombre de usuario ya existe") {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        TextButton(
            onClick = onVolverLogin,
            enabled = !enviando
        ) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
    }
}
