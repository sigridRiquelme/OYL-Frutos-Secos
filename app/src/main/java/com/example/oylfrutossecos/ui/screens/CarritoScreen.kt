
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
    cantidades: Map<Int, Int>,
    onCambiarCantidad: (Int, Int) -> Unit,
    onVolverCatalogo: () -> Unit,
    onConfirmarPedido: () -> Unit,
    modifier: Modifier = Modifier
) {

    // Productos que tienen unidades en el carrito
    val productosCarrito = productos.filter { producto ->
        (cantidades[producto.id] ?: 0) > 0
    }

    // Calculamos el total del pedido
    val total = productosCarrito.sumOf { producto ->
        producto.precio * (cantidades[producto.id] ?: 0)
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

        Spacer(modifier = Modifier.height(16.dp))

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

                    val cantidad = cantidades[producto.id] ?: 0
                    val subtotal = producto.precio * cantidad

                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            Text(
                                text = producto.nombre,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = "Precio: $${producto.precio}"
                            )

                            Text(
                                text = "Subtotal: $$subtotal"
                            )

                            Row(
                                verticalAlignment =
                                    Alignment.CenterVertically,
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
                                    enabled = cantidad > 1
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
                                    enabled = cantidad < producto.stock
                                ) {
                                    Text("+")
                                }
                            }

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
                Text("Continuar al pedido")
            }
        }
    }
}
