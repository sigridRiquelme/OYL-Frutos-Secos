
package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import com.example.oylfrutossecos.ui.viewmodel.ClienteViewModel
import com.example.oylfrutossecos.ui.viewmodel.EstadoRegistro

@Composable
fun RegistroScreen(
    clienteViewModel: ClienteViewModel,
    modifier: Modifier = Modifier,
    onVolverLogin: () -> Unit = {}
) {

    var nombre by remember { mutableStateOf("") }
    var nombreUsuario by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }

    var mostrarErrores by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }

    val estadoRegistro by clienteViewModel.estadoRegistro.collectAsState()

    LaunchedEffect(estadoRegistro) {
        if (estadoRegistro == EstadoRegistro.Exito) {
            clienteViewModel.limpiarEstado()
            onVolverLogin()
        }
    }

    val nombreInvalido = nombre.trim().length < 3
    val nombreUsuarioInvalido = nombreUsuario.trim().length < 3
    val correoInvalido = !android.util.Patterns
        .EMAIL_ADDRESS.matcher(correo).matches()
    val telefonoInvalido = telefono.count { it.isDigit() } < 9
    val contrasenaInvalida = contrasena.length < 6

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
            onValueChange = { nombre = it },
            label = { Text("Nombre completo") },
            modifier = Modifier.fillMaxWidth(),
            isError = mostrarErrores && nombreInvalido,
            supportingText = {
                if (mostrarErrores && nombreInvalido)
                    Text("Ingresa al menos 3 caracteres")
            },
            singleLine = true
        )


        OutlinedTextField(
            value = nombreUsuario,
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
                    Text("Ingresa al menos 3 caracteres")
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
            onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = mostrarErrores && contrasenaInvalida,
            supportingText = {
                if (mostrarErrores && contrasenaInvalida)
                    Text("Mínimo 6 caracteres")
            },
            singleLine = true
        )

        OutlinedTextField(
            value = direccion,
            onValueChange = { direccion = it },
            label = { Text("Dirección (opcional)") },
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
                    !contrasenaInvalida
                ) {
                    mensaje = ""

                    clienteViewModel.registrarCliente(
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
            enabled = estadoRegistro != EstadoRegistro.Cargando
        ) {
            Text(
                if (estadoRegistro == EstadoRegistro.Cargando)
                    "Guardando..."
                else
                    "Crear Cuenta"
            )
        }

        if (mensaje.isNotEmpty()) {
            Text(text = mensaje)
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
            onClick = onVolverLogin
        ) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
    }
}
