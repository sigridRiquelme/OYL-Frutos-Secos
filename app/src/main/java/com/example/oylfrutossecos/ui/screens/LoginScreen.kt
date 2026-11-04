package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oylfrutossecos.ui.viewmodel.ClienteViewModel
import com.example.oylfrutossecos.ui.viewmodel.EstadoLogin

private val VerdeLogin = Color(0xFF176243)

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
        modifier = modifier.fillMaxSize().background(Color(0xFFFAFAF5))
            .imePadding().verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("OYL", fontSize = 55.sp, fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp, color = VerdeLogin)
        Text("FRUTOS SECOS", fontSize = 14.sp, fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp, color = Color(0xFF465C4C))
        Spacer(Modifier.height(13.dp))
        Text("¡Qué bueno tenerte de vuelta!", fontSize = 22.sp,
            fontWeight = FontWeight.Bold, color = Color(0xFF203529), textAlign = TextAlign.Center)
        Text("Ingresa para continuar con tus compras",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF64796D), textAlign = TextAlign.Center)
        Spacer(Modifier.height(30.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(25.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Tipo de usuario", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    listOf("Cliente", "Administrador").forEach { opcion ->
                        FilterChip(
                            selected = rol == opcion,
                            onClick = {
                                rol = opcion
                                mensajeError = ""
                                clienteViewModel.limpiarEstadoLogin()
                            },
                            modifier = Modifier.weight(1f),
                            label = { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text(opcion, maxLines = 1, fontSize = 13.sp)
                            } },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE0F0E5),
                                selectedLabelColor = VerdeLogin
                            )
                        )
                    }
                }
                OutlinedTextField(
                    value = nombreUsuario,
                    onValueChange = {
                        nombreUsuario = it
                        mensajeError = ""
                        clienteViewModel.limpiarEstadoLogin()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre de usuario") },
                    placeholder = { Text("Escribe tu usuario") },
                    singleLine = true,
                    shape = RoundedCornerShape(13.dp)
                )
                OutlinedTextField(
                    value = contrasena,
                    onValueChange = {
                        contrasena = it
                        mensajeError = ""
                        clienteViewModel.limpiarEstadoLogin()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Contraseña") },
                    placeholder = { Text("Escribe tu contraseña") },
                    singleLine = true,
                    shape = RoundedCornerShape(13.dp),
                    visualTransformation = if (mostrarContrasena)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(onClick = { mostrarContrasena = !mostrarContrasena }) {
                            Text(if (mostrarContrasena) "Ocultar" else "Ver", color = VerdeLogin)
                        }
                    }
                )

                if (mensajeError.isNotBlank()) {
                    Text(mensajeError, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }
                if (rol == "Cliente" && estadoLogin is EstadoLogin.Error) {
                    Text((estadoLogin as EstadoLogin.Error).mensaje,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }
                Button(
                    onClick = {
                        mensajeError = ""
                        if (nombreUsuario.isBlank() || contrasena.isBlank()) {
                            mensajeError = "Ingresa tu nombre de usuario y contraseña."
                        } else if (rol == "Cliente") {
                            clienteViewModel.iniciarSesion(nombreUsuario.trim(), contrasena)
                        } else {
                            if (!onIngresar(rol, nombreUsuario.trim(), contrasena)) {
                                mensajeError = "Usuario o contraseña incorrectos."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(51.dp),
                    enabled = estadoLogin != EstadoLogin.Cargando,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerdeLogin, contentColor = Color.White,
                        disabledContainerColor = Color(0xFF90A99A),
                        disabledContentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(if (estadoLogin == EstadoLogin.Cargando && rol == "Cliente")
                        "Ingresando…" else "Iniciar sesión", fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("¿Aún no tienes cuenta? ", color = Color(0xFF64796D))
            Text("Regístrate", color = VerdeLogin,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onRegistrarse() })
        }
    }
}
