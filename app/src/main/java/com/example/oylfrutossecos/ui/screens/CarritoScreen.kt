
package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CarritoScreen(
    productos: List<ProductoCatalogo>,
    productosActuales: List<ProductoCatalogo>,
    cantidades: Map<Int, Int>,
    onCambiarCantidad: (Int, Int) -> Unit,
    onVolverCatalogo: () -> Unit,
    onConfirmarPedido: () -> Unit,
    modifier: Modifier = Modifier
) {

    // Datos más recientes recuperados desde Room
    val actualesPorId = productosActuales.associateBy {
        it.id
    }

    // Productos que están guardados en el carrito
    val productosCarrito = productos.filter { producto ->
        (cantidades[producto.id] ?: 0) > 0
    }

    // Total calculado con los precios actuales
    val total = productosCarrito.sumOf { producto ->

        val precioActual =
            actualesPorId[producto.id]?.precio ?: producto.precio

        val cantidad = cantidades[producto.id] ?: 0

        precioActual.toLong() * cantidad
    }

    // Detectar cambios que necesitan revisión
    val requiereRevision = productosCarrito.any { producto ->

        val actual = actualesPorId[producto.id]
        val cantidad = cantidades[producto.id] ?: 0

        actual == null ||
                !actual.activo ||
                cantidad > actual.stock ||
                actual.precio != producto.precio
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        TextButton(
            onClick = onVolverCatalogo
        ) {
            Text("← Volver al catálogo")
        }

        Text(
            text = "Mi carrito",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (requiereRevision) {

            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        text = "⚠️ Revisa tu carrito",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Algunos productos cambiaron. " +
                                "Revisa las advertencias antes de continuar."
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (productosCarrito.isEmpty()) {

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),

                contentAlignment = Alignment.Center
            ) {

                Text("Tu carrito está vacío.")
            }

        } else {

            LazyColumn(
                modifier = Modifier.weight(1f),

                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(
                    items = productosCarrito,
                    key = { it.id }
                ) { producto ->

                    val actual = actualesPorId[producto.id]

                    val cantidad = cantidades[producto.id] ?: 0

                    val precioActual =
                        actual?.precio ?: producto.precio

                    val stockActual = actual?.stock ?: 0

                    val disponible =
                        actual != null && actual.activo

                    val cambioPrecio =
                        actual != null &&
                                actual.precio != producto.precio

                    val stockInsuficiente =
                        disponible && cantidad > stockActual

                    val subtotal =
                        precioActual.toLong() * cantidad

                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp),

                            verticalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            // FOTOGRAFÍA
                            val imagenUri =
                                actual?.imagenUri ?: producto.imagenUri

                            if (!imagenUri.isNullOrBlank()) {

                                ImagenProductoVistaPrevia(
                                    imagenUri = imagenUri,

                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                )
                            }

                            Text(
                                text = actual?.nombre ?: producto.nombre,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = "Precio: $${precioActual}"
                            )

                            Text(
                                text = "Subtotal: $$subtotal"
                            )

                            // ADVERTENCIAS
                            if (actual == null) {

                                Text(
                                    text = "Este producto ya no existe. " +
                                            "Debes quitarlo del carrito.",
                                    color = MaterialTheme.colorScheme.error
                                )

                            } else if (!actual.activo) {

                                Text(
                                    text = "Este producto está desactivado " +
                                            "y no puede comprarse.",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            if (stockInsuficiente) {

                                Text(
                                    text = "Stock insuficiente. " +
                                            "Solicitaste $cantidad unidades, " +
                                            "pero solo hay $stockActual.",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            if (cambioPrecio) {

                                Text(
                                    text = "El precio cambió de " +
                                            "$${producto.precio} a " +
                                            "$${precioActual}. Revisa el nuevo total.",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            // CONTROLES DE CANTIDAD
                            Row(
                                verticalAlignment = Alignment.CenterVertically,

                                horizontalArrangement =
                                    Arrangement.spacedBy(12.dp)
                            ) {

                                OutlinedButton(
                                    onClick = {
                                        onCambiarCantidad(
                                            producto.id,
                                            cantidad - 1
                                        )
                                    },

                                    enabled = cantidad > 1 && disponible
                                ) {
                                    Text("−")
                                }

                                Text("$cantidad")

                                OutlinedButton(
                                    onClick = {
                                        onCambiarCantidad(
                                            producto.id,
                                            cantidad + 1
                                        )
                                    },

                                    enabled = disponible &&
                                            cantidad < stockActual
                                ) {
                                    Text("+")
                                }
                            }

                            // QUITAR PRODUCTO
                            TextButton(
                                onClick = {
                                    onCambiarCantidad(
                                        producto.id,
                                        0
                                    )
                                }
                            ) {
                                Text("Eliminar producto")
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Total: $$total",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onConfirmarPedido,
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    if (requiereRevision) {
                        "Revisar cambios y continuar"
                    } else {
                        "Continuar al pedido"
                    }
                )
            }
        }
    }
}
