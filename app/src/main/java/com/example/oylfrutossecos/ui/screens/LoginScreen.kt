package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.oylfrutossecos.ui.viewmodel.ClienteViewModel
import com.example.oylfrutossecos.ui.viewmodel.EstadoLogin

@Composable
fun LoginScreen(
    clienteViewModel: ClienteViewModel,
    modifier: Modifier = Modifier,
    onRegistrarse: () -> Unit = {},
    onIngresar: (String, String, String) -> Boolean = { _, _, _ -> false },
    onClienteAutenticado: (Int) -> Unit = {}
) {
    var nombreUsuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var mostrarContrasena by remember { mutableStateOf(false) }
    var rol by remember { mutableStateOf("Cliente") }
    var mensajeError by remember { mutableStateOf("") }

    val estadoLogin by clienteViewModel.estadoLogin.collectAsState()

    LaunchedEffect(estadoLogin) {
        val estado = estadoLogin
        if (estado is EstadoLogin.Exito) {
            clienteViewModel.limpiarEstadoLogin()
            onClienteAutenticado(estado.clienteId)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "OYL Frutos Secos",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Selecciona tu tipo de usuario",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = rol == "Cliente",
                    onClick = {
                        rol = "Cliente"
                        mensajeError = ""
                        clienteViewModel.limpiarEstadoLogin()
                    }
                )
                Text("Cliente")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = rol == "Administrador",
                    onClick = {
                        rol = "Administrador"
                        mensajeError = ""
                        clienteViewModel.limpiarEstadoLogin()
                    }
                )
                Text("Administrador")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nombreUsuario,
            onValueChange = {
                nombreUsuario = it
                mensajeError = ""
                clienteViewModel.limpiarEstadoLogin()
            },
            label = { Text("Nombre de usuario") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = {
                contrasena = it
                mensajeError = ""
                clienteViewModel.limpiarEstadoLogin()
            },
            label = { Text("Contraseña") },
            visualTransformation = if (mostrarContrasena) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                TextButton(onClick = { mostrarContrasena = !mostrarContrasena }) {
                    Text(if (mostrarContrasena) "Ocultar" else "Ver")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                mensajeError = ""

                if (nombreUsuario.isBlank() || contrasena.isBlank()) {
                    mensajeError = "Ingresa tu nombre de usuario y contraseña"
                } else if (rol == "Cliente") {
                    clienteViewModel.iniciarSesion(
                        nombreUsuario = nombreUsuario.trim(),
                        contrasena = contrasena
                    )
                } else {
                    val accesoCorrecto = onIngresar(
                        rol,
                        nombreUsuario.trim(),
                        contrasena
                    )
                    if (!accesoCorrecto) {
                        mensajeError = "Nombre de usuario o contraseña incorrectos"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = estadoLogin != EstadoLogin.Cargando
        ) {
            Text(
                if (estadoLogin == EstadoLogin.Cargando && rol == "Cliente") {
                    "Ingresando..."
                } else {
                    "Ingresar"
                }
            )
        }

        if (mensajeError.isNotEmpty()) {
            Text(
                text = mensajeError,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (rol == "Cliente" && estadoLogin is EstadoLogin.Error) {
            Text(
                text = (estadoLogin as EstadoLogin.Error).mensaje,
                color = MaterialTheme.colorScheme.error
            )
        }

        TextButton(onClick = onRegistrarse) {
            Text("¿No tienes cuenta? Regístrate")
        }
    }
}
