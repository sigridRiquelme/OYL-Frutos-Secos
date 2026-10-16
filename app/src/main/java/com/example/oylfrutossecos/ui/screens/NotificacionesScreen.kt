
package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.oylfrutossecos.data.local.PedidoLocal

// Información que necesita cada aviso
private data class AvisoOyl(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val filtroDestino: String
)

@Composable
fun NotificacionesScreen(
    productos: List<ProductoCatalogo>,
    estadosProductos: Map<Int, Boolean>,
    pedidos: List<PedidoLocal>,
    onVolver: () -> Unit,
    onAbrirInventario: (String) -> Unit,
    onAbrirPedidos: () -> Unit,
    modifier: Modifier = Modifier
) {

    var filtroSeleccionado by rememberSaveable {
        mutableStateOf("Todas")
    }

    // AVISOS DE INVENTARIO
    // Solamente considerar productos activos
    val avisosInventario = productos
        .filter { producto ->

            val activo = estadosProductos[producto.id] == true

            activo && producto.stock in 0..10
        }
        .sortedWith(
            compareBy<ProductoCatalogo> {
                if (it.stock == 0) 0 else 1
            }.thenBy {
                it.nombre.lowercase()
            }
        )
        .map { producto ->

            val agotado = producto.stock == 0

            AvisoOyl(
                id = "stock-${producto.id}",

                titulo = if (agotado) {
                    "Producto agotado"
                } else {
                    "Stock bajo"
                },

                descripcion = if (agotado) {
                    "${producto.nombre} no tiene unidades disponibles."
                } else {
                    "${producto.nombre} tiene " +
                            "${producto.stock} unidades disponibles."
                },

                categoria = "Inventario",

                filtroDestino = if (agotado) {
                    "Agotados"
                } else {
                    "Stock bajo"
                }
            )
        }

    // AVISOS DE PEDIDOS
    // Solo pedidos que esperan atención
    val avisosPedidos = pedidos
        .filter {
            it.estado == "Pendiente"
        }
        .sortedByDescending {
            it.fecha
        }
        .map { pedido ->

            AvisoOyl(
                id = "pedido-${pedido.numero}",

                titulo = "Pedido pendiente",

                descripcion =
                    "El pedido ${pedido.numero} " +
                            "espera atención. " +
                            "Modalidad: ${pedido.modalidad}.",

                categoria = "Pedidos",

                filtroDestino = "Pendiente"
            )
        }

    // Todos los avisos activos
    val avisos = avisosPedidos + avisosInventario

    val avisosFiltrados = avisos.filter { aviso ->

        filtroSeleccionado == "Todas" ||
                aviso.categoria == filtroSeleccionado
    }

    val cantidadAgotados = productos.count {
        estadosProductos[it.id] == true && it.stock == 0
    }

    val cantidadStockBajo = productos.count {
        estadosProductos[it.id] == true && it.stock in 1..10
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),

        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // VOLVER
        TextButton(
            onClick = onVolver
        ) {
            Text("← Volver al inicio")
        }

        // TÍTULO
        Text(
            "Notificaciones",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            "Avisos activos de pedidos e inventario.",
            style = MaterialTheme.typography.bodyMedium
        )

        // RESUMEN DE AVISOS
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(16.dp),

                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    "Resumen de avisos",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    "Total de avisos: ${avisos.size}"
                )

                Text(
                    "Pedidos pendientes: ${avisosPedidos.size}"
                )

                Text(
                    "Productos agotados: $cantidadAgotados"
                )

                Text(
                    "Productos con stock bajo: $cantidadStockBajo"
                )
            }
        }

        // FILTROS
        Row(
            modifier = Modifier.horizontalScroll(
                rememberScrollState()
            ),

            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            listOf(
                "Todas",
                "Pedidos",
                "Inventario"
            ).forEach { filtro ->

                FilterChip(
                    selected = filtroSeleccionado == filtro,

                    onClick = {
                        filtroSeleccionado = filtro
                    },

                    label = {
                        Text(filtro)
                    }
                )
            }
        }

        // LISTADO
        LazyColumn(
            modifier = Modifier.weight(1f),

            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (avisosFiltrados.isEmpty()) {

                item {

                    Text(
                        text = if (avisos.isEmpty()) {
                            "No tienes avisos activos."
                        } else {
                            "No hay avisos en esta categoría."
                        },

                        modifier = Modifier.padding(16.dp)
                    )
                }

            } else {

                items(
                    items = avisosFiltrados,
                    key = { it.id }
                ) { aviso ->

                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp),

                            verticalArrangement =
                                Arrangement.spacedBy(10.dp)
                        ) {

                            Text(
                                text = if (
                                    aviso.categoria == "Pedidos"
                                ) {
                                    "📦 ${aviso.titulo}"
                                } else if (
                                    aviso.titulo == "Producto agotado"
                                ) {
                                    "🔴 ${aviso.titulo}"
                                } else {
                                    "⚠️ ${aviso.titulo}"
                                },

                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                aviso.descripcion,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Button(
                                onClick = {

                                    if (
                                        aviso.categoria == "Inventario"
                                    ) {

                                        onAbrirInventario(
                                            aviso.filtroDestino
                                        )

                                    } else {

                                        onAbrirPedidos()
                                    }
                                },

                                modifier = Modifier.fillMaxWidth()
                            ) {

                                Text(
                                    if (
                                        aviso.categoria == "Inventario"
                                    ) {
                                        "Ver inventario"
                                    } else {
                                        "Ver pedidos pendientes"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
