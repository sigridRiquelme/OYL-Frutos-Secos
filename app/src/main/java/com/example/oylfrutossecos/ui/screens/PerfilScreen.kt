
package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.runtime.*
import com.example.oylfrutossecos.ui.viewmodel.ClienteViewModel
import com.example.oylfrutossecos.ui.viewmodel.EstadoEdicionPerfil

import com.example.oylfrutossecos.data.local.entity.ClienteEntity

@Composable
fun PerfilScreen(
    cliente: ClienteEntity?,
    clienteViewModel: ClienteViewModel,
    modifier: Modifier = Modifier
) {

    var editando by remember { mutableStateOf(false) }

    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }

    val estadoEdicion by clienteViewModel.estadoEdicionPerfil.collectAsState()

    LaunchedEffect(cliente?.id, editando) {
        if (editando && cliente != null) {
            nombre = cliente.nombre
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
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Mi perfil",
            style = MaterialTheme.typography.headlineMedium
        )


        if (cliente == null) {

            Text("No se pudo cargar la información del cliente.")

        } else if (editando) {

            Text(
                text = "Editar información personal",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre completo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // El nombre de usuario no se puede modificar
            DatosPerfil(
                titulo = "Nombre de usuario",
                valor = cliente.nombreUsuario
            )

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth()
            )

            if (estadoEdicion is EstadoEdicionPerfil.Error) {
                Text(
                    text = (estadoEdicion as EstadoEdicionPerfil.Error).mensaje,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    clienteViewModel.actualizarPerfil(
                        nombre = nombre,
                        correo = correo,
                        telefono = telefono,
                        direccion = direccion
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = estadoEdicion != EstadoEdicionPerfil.Guardando
            ) {
                Text(
                    if (estadoEdicion == EstadoEdicionPerfil.Guardando)
                        "Guardando..."
                    else
                        "Guardar cambios"
                )
            }

            OutlinedButton(
                onClick = {
                    editando = false
                    clienteViewModel.limpiarEstadoEdicionPerfil()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = estadoEdicion != EstadoEdicionPerfil.Guardando
            ) {
                Text("Cancelar")
            }

        } else {

            Text(
                text = "Información personal",
                style = MaterialTheme.typography.titleMedium
            )

            DatosPerfil("Nombre completo", cliente.nombre)
            DatosPerfil("Nombre de usuario", cliente.nombreUsuario)
            DatosPerfil("Correo electrónico", cliente.correo)
            DatosPerfil("Teléfono", cliente.telefono)

            DatosPerfil(
                "Dirección",
                cliente.direccion.ifBlank {
                    "Sin dirección registrada"
                }
            )

            Button(
                onClick = {
                    clienteViewModel.limpiarEstadoEdicionPerfil()
                    editando = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Editar perfil")
            }
        }

    }
}

@Composable
private fun DatosPerfil(
    titulo: String,
    valor: String
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelMedium
            )

            Text(
                text = valor,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
