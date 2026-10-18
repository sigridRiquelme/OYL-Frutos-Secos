package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.oylfrutossecos.data.local.PedidoLocal
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisPedidosScreen(
    pedidos: List<PedidoLocal>,
    clienteId: Int?,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Filtramos los pedidos exclusivamente del cliente autenticado
    val misPedidos = pedidos.filter { it.clienteId == clienteId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Pedidos") },
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Text("← Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        if (misPedidos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no tienes pedidos registrados.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(misPedidos) { pedido ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = pedido.numero,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Surface(
                                    color = when (pedido.estado) {
                                        "Pendiente" -> MaterialTheme.colorScheme.errorContainer
                                        "Entregado" -> MaterialTheme.colorScheme.primaryContainer
                                        else -> MaterialTheme.colorScheme.secondaryContainer
                                    },
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = pedido.estado,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }

                            val fechaFormateada = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                .format(Date(pedido.fecha))

                            Text(
                                text = "Fecha: $fechaFormateada",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = "Modalidad: ${pedido.modalidad} (${pedido.direccion.ifBlank { "Retiro en tienda" }})",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            HorizontalDivider()

                            Text(
                                text = "Productos:",
                                style = MaterialTheme.typography.labelLarge
                            )

                            pedido.productos.forEach { detalle ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${detalle.cantidad}x ${detalle.nombre}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "$${detalle.precioUnitario * detalle.cantidad}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            HorizontalDivider()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total:",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "$${pedido.total}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}