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
        modifier = modifier.fillMaxSize()
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
                isError = nombre.trim().length < 3,
                supportingText = { if (nombre.trim().length < 3) Text("Minimo 3 caracteres") }
            )
            OutlinedTextField(
                value = nombreUsuario,
                onValueChange = {
                    nombreUsuario = it
                    clienteViewModel.limpiarEstadoEdicionPerfil()
                },
                label = { Text("Nombre de usuario") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                isError = nombreUsuario.trim().length < 3 ||
                    (estadoEdicion is EstadoEdicionPerfil.Error &&
                        (estadoEdicion as EstadoEdicionPerfil.Error).mensaje.contains("usuario ya existe")),
                supportingText = {
                    if (nombreUsuario.trim().length < 3) Text("Minimo 3 caracteres")
                    else if (estadoEdicion is EstadoEdicionPerfil.Error &&
                        (estadoEdicion as EstadoEdicionPerfil.Error).mensaje.contains("usuario ya existe"))
                        Text("El nombre de usuario ya existe")
                }
            )
            OutlinedTextField(
                value = correo, onValueChange = { correo = it },
                label = { Text("Correo electronico") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = telefono, onValueChange = { telefono = it },
                label = { Text("Telefono") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true
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
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Hola, ${cliente.nombre.substringBefore(' ')}!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                    Text("@${cliente.nombreUsuario}",
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
            DatosPerfil("Nombre completo", cliente.nombre)
            DatosPerfil("Nombre de usuario", cliente.nombreUsuario)
            DatosPerfil("Correo electronico", cliente.correo)
            DatosPerfil("Telefono", cliente.telefono)
            DatosPerfil("Direccion", cliente.direccion.ifBlank { "Sin direccion registrada" })

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
