
package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.oylfrutossecos.data.local.PedidoLocal

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Convertir una cantidad a pesos chilenos
private fun formatoDinero(valor: Int): String {
    return "$" + String.format(
        Locale.forLanguageTag("es-CL"),
        "%,d",
        valor
    )
}

// Mostrar fecha y hora del pedido
private fun formatoFecha(fecha: Long): String {
    return SimpleDateFormat(
        "dd/MM/yyyy HH:mm",
        Locale.forLanguageTag("es-CL")
    ).format(Date(fecha))
}

// Opciones permitidas para cambiar el estado
private fun estadosSiguientes(
    pedido: PedidoLocal
): List<String> {

    return when (pedido.estado) {

        "Pendiente" ->
            listOf("En preparación", "Cancelado")

        "En preparación" ->
            if (pedido.modalidad == "Retiro") {
                listOf("Listo para retiro", "Cancelado")
            } else {
                listOf("En despacho", "Cancelado")
            }

        "Listo para retiro", "En despacho" ->
            listOf("Entregado")

        else -> emptyList()
    }
}

@Composable
fun GestionPedidosScreen(
    pedidos: List<PedidoLocal>,
    onVolver: () -> Unit,
    onCambiarEstado: (
        String,
        String,
        (String?) -> Unit
    ) -> Unit,
    modifier: Modifier = Modifier,
    filtroInicial: String = "Todos"
) {


    var busqueda by rememberSaveable {
        mutableStateOf("")
    }

    var filtroSeleccionado by rememberSaveable(filtroInicial) {
        mutableStateOf(filtroInicial)
    }


    var numeroPedidoSeleccionado by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var estadoPorConfirmar by remember {
        mutableStateOf<String?>(null)
    }

    var guardando by remember {
        mutableStateOf(false)
    }

    var errorEstado by remember {
        mutableStateOf<String?>(null)
    }

    // Buscar el pedido actualizado
    val pedidoSeleccionado = pedidos.firstOrNull {
        it.numero == numeroPedidoSeleccionado
    }

    // Resumen
    val totalPendientes = pedidos.count {
        it.estado == "Pendiente"
    }

    val totalEnProceso = pedidos.count {
        it.estado == "En preparación" ||
                it.estado == "Listo para retiro" ||
                it.estado == "En despacho"
    }

    // Buscar, filtrar y ordenar
    val pedidosFiltrados = pedidos.filter { pedido ->

        val coincideBusqueda =
            pedido.numero.contains(busqueda, ignoreCase = true) ||
                    pedido.nombreCliente.contains(busqueda, ignoreCase = true) ||
                    pedido.telefono.contains(busqueda)

        val coincideFiltro =
            filtroSeleccionado == "Todos" ||
                    pedido.estado == filtroSeleccionado

        coincideBusqueda && coincideFiltro

    }.sortedByDescending {
        it.fecha
    }

    // Diálogo de información del pedido
    if (pedidoSeleccionado != null) {

        val nuevoEstado = estadoPorConfirmar

        if (nuevoEstado != null) {

            // Confirmación antes de cambiar estado
            AlertDialog(
                onDismissRequest = {
                    if (!guardando) {
                        estadoPorConfirmar = null
                    }
                },

                title = {
                    Text("Confirmar cambio")
                },

                text = {
                    Text(
                        "¿Deseas cambiar el pedido " +
                                "${pedidoSeleccionado.numero} " +
                                "a \"$nuevoEstado\"?"
                    )
                },

                confirmButton = {
                    Button(
                        onClick = {

                            guardando = true
                            errorEstado = null

                            onCambiarEstado(
                                pedidoSeleccionado.numero,
                                nuevoEstado
                            ) { error ->

                                guardando = false
                                estadoPorConfirmar = null

                                if (error != null) {
                                    errorEstado = error
                                }
                            }
                        },

                        enabled = !guardando
                    ) {
                        Text(
                            if (guardando) {
                                "Guardando..."
                            } else {
                                "Confirmar"
                            }
                        )
                    }
                },

                dismissButton = {
                    TextButton(
                        onClick = {
                            estadoPorConfirmar = null
                        },

                        enabled = !guardando
                    ) {
                        Text("Cancelar")
                    }
                }
            )

        } else {

            AlertDialog(
                onDismissRequest = {
                    numeroPedidoSeleccionado = null
                    errorEstado = null
                },

                title = {
                    Text("Detalle del pedido")
                },

                text = {

                    Column(
                        modifier = Modifier
                            .heightIn(max = 450.dp)
                            .verticalScroll(rememberScrollState()),

                        verticalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {

                        Text(
                            pedidoSeleccionado.numero,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Text(
                            "Fecha: ${formatoFecha(pedidoSeleccionado.fecha)}"
                        )

                        Text(
                            "Estado: ${pedidoSeleccionado.estado}"
                        )

                        HorizontalDivider()

                        Text(
                            "Cliente: ${pedidoSeleccionado.nombreCliente}"
                        )

                        Text(
                            "Teléfono: ${pedidoSeleccionado.telefono}"
                        )

                        Text(
                            "Modalidad: ${pedidoSeleccionado.modalidad}"
                        )

                        if (pedidoSeleccionado.modalidad == "Despacho") {
                            Text(
                                "Dirección: ${pedidoSeleccionado.direccion}"
                            )
                        }

                        HorizontalDivider()

                        Text(
                            "Productos solicitados",
                            style = MaterialTheme.typography.titleMedium
                        )

                        pedidoSeleccionado.productos.forEach { producto ->

                            val subtotal =
                                producto.precioUnitario * producto.cantidad

                            Text(
                                "${producto.nombre} x ${producto.cantidad}"
                            )

                            Text(
                                "Subtotal: ${formatoDinero(subtotal)}"
                            )
                        }

                        HorizontalDivider()

                        Text(
                            "Total: ${formatoDinero(pedidoSeleccionado.total)}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        if (errorEstado != null) {
                            Text(
                                errorEstado.orEmpty(),
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        val opciones =
                            estadosSiguientes(pedidoSeleccionado)

                        if (opciones.isNotEmpty()) {

                            HorizontalDivider()

                            Text(
                                "Actualizar estado",
                                style = MaterialTheme.typography.titleMedium
                            )

                            opciones.forEach { estado ->

                                if (estado == "Cancelado") {

                                    OutlinedButton(
                                        onClick = {
                                            estadoPorConfirmar = estado
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Cancelar pedido")
                                    }

                                } else {

                                    Button(
                                        onClick = {
                                            estadoPorConfirmar = estado
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Cambiar a: $estado")
                                    }
                                }
                            }

                        } else {

                            Text(
                                "Este pedido no tiene cambios pendientes."
                            )
                        }
                    }
                },

                confirmButton = {
                    TextButton(
                        onClick = {
                            numeroPedidoSeleccionado = null
                            errorEstado = null
                        }
                    ) {
                        Text("Cerrar")
                    }
                }
            )
        }
    }

    // Pantalla principal
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),

        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        TextButton(
            onClick = onVolver
        ) {
            Text("← Volver al inicio")
        }

        Text(
            "Gestión de Pedidos",
            style = MaterialTheme.typography.headlineMedium
        )

        // Resumen de pedidos
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    "Resumen de pedidos",
                    style = MaterialTheme.typography.titleMedium
                )

                Text("Total registrados: ${pedidos.size}")

                Text("Pendientes: $totalPendientes")

                Text("En proceso: $totalEnProceso")
            }
        }

        // Buscador
        OutlinedTextField(
            value = busqueda,

            onValueChange = {
                busqueda = it
            },

            label = {
                Text("Buscar pedido o cliente")
            },

            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Filtros por estado
        Row(
            modifier = Modifier.horizontalScroll(
                rememberScrollState()
            ),

            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            listOf(
                "Todos",
                "Pendiente",
                "En preparación",
                "Listo para retiro",
                "En despacho",
                "Entregado",
                "Cancelado"
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

        // Listado de pedidos
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (pedidosFiltrados.isEmpty()) {

                item {

                    Text(
                        text = if (pedidos.isEmpty()) {
                            "Todavía no hay pedidos registrados."
                        } else {
                            "No se encontraron pedidos con este filtro."
                        },

                        modifier = Modifier.padding(16.dp)
                    )
                }

            } else {

                items(
                    items = pedidosFiltrados,
                    key = { it.numero }
                ) { pedido ->

                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp),

                            verticalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            Text(
                                pedido.numero,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                "Cliente: ${pedido.nombreCliente}"
                            )

                            Text(
                                "Fecha: ${formatoFecha(pedido.fecha)}"
                            )

                            Text(
                                "Modalidad: ${pedido.modalidad}"
                            )

                            Text(
                                "Total: ${formatoDinero(pedido.total)}"
                            )

                            Text(
                                "Estado: ${pedido.estado}",
                                color =
                                    if (pedido.estado == "Cancelado") {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                            )

                            Button(
                                onClick = {
                                    numeroPedidoSeleccionado = pedido.numero
                                    errorEstado = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Ver detalles")
                            }
                        }
                    }
                }
            }
        }
    }
}
