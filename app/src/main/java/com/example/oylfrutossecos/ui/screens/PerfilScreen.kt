package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.oylfrutossecos.ui.viewmodel.ClienteViewModel
import com.example.oylfrutossecos.ui.viewmodel.EstadoEdicionPerfil
import com.example.oylfrutossecos.data.local.entity.ClienteEntity
import com.example.oylfrutossecos.feature.autenticacion.domain.ValidadorRegistro
import com.example.oylfrutossecos.feature.autenticacion.platform.ValidadorCorreoAndroid

@Composable
fun PerfilScreen(
    cliente: ClienteEntity?,
    clienteViewModel: ClienteViewModel,
    onCerrarSesion: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var editando by remember { mutableStateOf(false) }
    var nombre by remember { mutableStateOf("") }
    var nombreUsuario by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    val estadoEdicion by clienteViewModel.estadoEdicionPerfil.collectAsState()

    LaunchedEffect(cliente?.id, editando) {
        if (editando && cliente != null) {
            nombre = cliente.nombre
            nombreUsuario = cliente.nombreUsuario
            correo = cliente.correo
            telefono = cliente.telefono
            direccion = cliente.direccion
        }
    }
    LaunchedEffect(estadoEdicion) {
        if (estadoEdicion == EstadoEdicionPerfil.Exito) {
            editando = false
            clienteViewModel.limpiarEstadoEdicionPerfil()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Mi perfil", style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold)

        if (cliente == null) {
            Text("No se pudo cargar la informacion del cliente.")
        } else if (editando) {
            Text("Editar informacion personal", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = nombre, onValueChange = { nombre = it },
                label = { Text("Nombre completo") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                isError = !ValidadorRegistro.esNombreValido(nombre),
                supportingText = { if (!ValidadorRegistro.esNombreValido(nombre)) Text("Minimo 3 caracteres") }
            )
            OutlinedTextField(
                value = nombreUsuario,
                onValueChange = {
                    nombreUsuario = it
                    clienteViewModel.limpiarEstadoEdicionPerfil()
                },
                label = { Text("Nombre de usuario") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                isError = !ValidadorRegistro.esUsuarioValido(nombreUsuario) ||
                    (estadoEdicion is EstadoEdicionPerfil.Error &&
                        (estadoEdicion as EstadoEdicionPerfil.Error).mensaje.contains("usuario ya existe")),
                supportingText = {
                    if (!ValidadorRegistro.esUsuarioValido(nombreUsuario)) Text("Minimo 3 caracteres")
                    else if (estadoEdicion is EstadoEdicionPerfil.Error &&
                        (estadoEdicion as EstadoEdicionPerfil.Error).mensaje.contains("usuario ya existe"))
                        Text("El nombre de usuario ya existe")
                }
            )

            OutlinedTextField(
                value = correo,
                onValueChange = {
                    correo = it
                    clienteViewModel.limpiarEstadoEdicionPerfil()
                },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,

                // Marcar el campo si el correo no es válido
                isError = !ValidadorCorreoAndroid.esValido(
                    correo.trim()
                ),

                // Mostrar el mensaje de error
                supportingText = {
                    if (!ValidadorCorreoAndroid.esValido(correo.trim())) {
                        Text("Ingresa un correo electrónico válido")
                    }
                }
            )


            OutlinedTextField(
                value = telefono,
                onValueChange = {
                    telefono = it
                    clienteViewModel.limpiarEstadoEdicionPerfil()
                },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,

                // Marcar el campo si el teléfono no es válido
                isError = !ValidadorRegistro.esTelefonoValido(
                    telefono
                ),

                // Mostrar el mensaje de error
                supportingText = {
                    if (!ValidadorRegistro.esTelefonoValido(telefono)) {
                        Text("El teléfono debe tener al menos 9 dígitos")
                    }
                }
            )

            OutlinedTextField(
                value = direccion, onValueChange = { direccion = it },
                label = { Text("Direccion") }, modifier = Modifier.fillMaxWidth()
            )

            if (estadoEdicion is EstadoEdicionPerfil.Error) {
                Text((estadoEdicion as EstadoEdicionPerfil.Error).mensaje,
                    color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = {
                    clienteViewModel.actualizarPerfil(
                        nombre = nombre,
                        nombreUsuario = nombreUsuario,
                        correo = correo,
                        telefono = telefono,
                        direccion = direccion
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = estadoEdicion != EstadoEdicionPerfil.Guardando
            ) {
                Text(if (estadoEdicion == EstadoEdicionPerfil.Guardando)
                    "Guardando..." else "Guardar cambios")
            }
            OutlinedButton(
                onClick = {
                    editando = false
                    clienteViewModel.limpiarEstadoEdicionPerfil()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = estadoEdicion != EstadoEdicionPerfil.Guardando
            ) { Text("Cancelar") }
        } else {
            ResumenPerfilOYL(cliente.nombre, cliente.nombreUsuario, cliente.correo, cliente.telefono, cliente.direccion)

            Button(
                onClick = {
                    clienteViewModel.limpiarEstadoEdicionPerfil()
                    editando = true
                }, modifier = Modifier.fillMaxWidth()
            ) { Text("Editar perfil") }
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(
                onClick = onCerrarSesion,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) { Text("Cerrar sesion") }
        }
    }
}

@Composable
internal fun ResumenPerfilOYL(
    nombre: String,
    usuario: String,
    correo: String,
    telefono: String,
    direccion: String
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "Hola, ${nombre.substringBefore(' ')}!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text("@$usuario", style = MaterialTheme.typography.bodyMedium)
        }
    }
    DatosPerfil("Nombre completo", nombre)
    DatosPerfil("Nombre de usuario", usuario)
    DatosPerfil("Correo electrónico", correo)
    DatosPerfil("Teléfono", telefono)
    DatosPerfil("Dirección", direccion.ifBlank { "Sin dirección registrada" })
}

@Composable
private fun DatosPerfil(titulo: String, valor: String) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(valor, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
