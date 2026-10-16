
package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DetalleProductoScreen(
    producto: ProductoCatalogo,
    rol: String,
    cantidadYaAgregada: Int,
    onVolver: () -> Unit,
    onAgregar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {

    val disponible = (
            producto.stock - cantidadYaAgregada
            ).coerceAtLeast(0)

    var cantidad by remember(
        producto.id,
        cantidadYaAgregada
    ) {
        mutableIntStateOf(
            if (disponible > 0) 1 else 0
        )
    }

    var confirmado by remember(producto.id) {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),

        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Botón para regresar al catálogo
        TextButton(
            onClick = onVolver
        ) {
            Text("← Volver al catálogo")
        }


// Mostrar la fotografía real del producto
        ImagenProductoVistaPrevia(
            imagenUri = producto.imagenUri,

            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )


        // Información del producto
        Text(
            text = producto.nombre,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = producto.categoria
        )

        Text(
            text = producto.presentacion
        )

        Text(
            text = "$${producto.precio}",
            fontSize = 25.sp,
            color = Color(0xFF1B5E20),
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Stock publicado: ${producto.stock}"
        )

        HorizontalDivider()

        Text(
            text = "Descripción",
            fontWeight = FontWeight.Bold
        )

        Text(
            text = producto.descripcion
        )

        // Opciones del cliente
        if (rol == "Cliente") {

            Text(
                text = "Disponible para agregar: $disponible"
            )

            Text(
                text = "Seleccionar cantidad",
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(16.dp)
            ) {

                OutlinedButton(
                    onClick = {
                        if (cantidad > 1) {
                            cantidad--
                        }
                    },
                    enabled = cantidad > 1
                ) {
                    Text("−")
                }

                Text(
                    text = "$cantidad",
                    fontSize = 22.sp
                )

                OutlinedButton(
                    onClick = {
                        if (cantidad < disponible) {
                            cantidad++
                        }
                    },
                    enabled = cantidad < disponible
                ) {
                    Text("+")
                }
            }

            Button(
                onClick = {
                    onAgregar(cantidad)
                    confirmado = true
                },
                enabled = cantidad > 0 &&
                        cantidad <= disponible,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar al carrito")
            }

            if (confirmado) {
                Text(
                    text = "Producto agregado al carrito temporal.",
                    color = Color(0xFF1B5E20)
                )
            }

            if (cantidadYaAgregada > 0) {
                Text(
                    text = "En tu carrito: $cantidadYaAgregada unidades"
                )
            }

        } else {

            Text(
                text = "La edición de este producto estará disponible en Gestión de productos."
            )
        }
    }
}
