
package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PerfilAdministradorScreen(
    productosActivos: Int,
    alertasStock: Int,
    pedidosPendientes: Int,
    pedidosRegistrados: Int,
    onVolver: () -> Unit,
    onGestionProductos: () -> Unit,
    onGestionInventario: () -> Unit,
    onGestionPedidos: () -> Unit,
    onNotificaciones: () -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {

    var mostrarConfirmacionSalida by rememberSaveable {
        mutableStateOf(false)
    }

    // Confirmación para evitar cerrar sesión accidentalmente
    if (mostrarConfirmacionSalida) {

        AlertDialog(
            onDismissRequest = {
                mostrarConfirmacionSalida = false
            },

            title = {
                Text("Cerrar sesión")
            },

            text = {
                Text(
                    "¿Estás seguro de que deseas cerrar " +
                            "la sesión del Administrador?"
                )
            },

            confirmButton = {

                Button(
                    onClick = {
                        mostrarConfirmacionSalida = false
                        onCerrarSesion()
                    }
                ) {
                    Text("Cerrar sesión")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        mostrarConfirmacionSalida = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),

        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        TextButton(
            onClick = onVolver
        ) {
            Text("← Volver al inicio")
        }

        Text(
            text = "Mi perfil",
            style = MaterialTheme.typography.headlineMedium
        )

        // INFORMACIÓN DE LA CUENTA
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(20.dp),

                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Text(
                    text = "👤 Administrador",
                    style = MaterialTheme.typography.headlineSmall
                )

                Text("Usuario: admin")

                Text("Rol: Administrador")

                Text(
                    text = "Cuenta de prueba académica",
                    color = MaterialTheme.colorScheme.primary
                )

                HorizontalDivider()

                Text(
                    text = "Administración de OYL Frutos Secos",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // RESUMEN REAL
        Text(
            text = "Resumen de actividad",
            style = MaterialTheme.typography.titleLarge
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            DatoPerfilAdministrador(
                titulo = "Productos activos",
                cantidad = productosActivos,
                modifier = Modifier.weight(1f)
            )

            DatoPerfilAdministrador(
                titulo = "Alertas de stock",
                cantidad = alertasStock,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            DatoPerfilAdministrador(
                titulo = "Pedidos pendientes",
                cantidad = pedidosPendientes,
                modifier = Modifier.weight(1f)
            )

            DatoPerfilAdministrador(
                titulo = "Pedidos registrados",
                cantidad = pedidosRegistrados,
                modifier = Modifier.weight(1f)
            )
        }

        // ACCESOS RÁPIDOS
        Text(
            text = "Accesos rápidos",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedButton(
            onClick = onGestionProductos,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📦 Gestionar productos")
        }

        OutlinedButton(
            onClick = onGestionInventario,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📊 Consultar inventario")
        }

        OutlinedButton(
            onClick = onGestionPedidos,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🧾 Gestionar pedidos")
        }

        OutlinedButton(
            onClick = onNotificaciones,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🔔 Ver notificaciones")
        }

        HorizontalDivider()

        // INFORMACIÓN DE LA APLICACIÓN
        Text(
            text = "Acerca de la aplicación",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "OYL Frutos Secos es una aplicación " +
                    "académica para gestionar productos, " +
                    "inventario y solicitudes de compra."
        )

        Text(
            text = "Los datos administrativos se almacenan " +
                    "localmente en este dispositivo.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider()

        // CERRAR SESIÓN
        Button(
            onClick = {
                mostrarConfirmacionSalida = true
            },

            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}


// TARJETA REUTILIZABLE PARA ESTADÍSTICAS
@Composable
private fun DatoPerfilAdministrador(
    titulo: String,
    cantidad: Int,
    modifier: Modifier = Modifier
) {

    ElevatedCard(
        modifier = modifier.heightIn(min = 105.dp)
    ) {

        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = cantidad.toString(),
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = titulo,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
