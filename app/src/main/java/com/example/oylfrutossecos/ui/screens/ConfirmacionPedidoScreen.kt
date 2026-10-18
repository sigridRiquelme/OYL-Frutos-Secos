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
fun ConfirmacionPedidoScreen(
    productos: List<ProductoCatalogo>,
    cantidades: Map<Int, Int>,
    onVolver: () -> Unit,
    onConfirmar: (String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {

    // Productos que están en el carrito
    val productosPedido = productos.filter { producto ->
        (cantidades[producto.id] ?: 0) > 0
    }

    // Calcular total
    val total = productosPedido.sumOf { producto ->
        producto.precio * (cantidades[producto.id] ?: 0)
    }

    // Datos del formulario
    var nombre by rememberSaveable {
        mutableStateOf("")
    }

    var telefono by rememberSaveable {
        mutableStateOf("")
    }

    var modalidad by rememberSaveable {
        mutableStateOf("Retiro")
    }

    var direccion by rememberSaveable {
        mutableStateOf("")
    }

    var mostrarErrores by rememberSaveable {
        mutableStateOf(false)
    }

    // Validaciones
    val nombreValido = nombre.trim().length >= 3

    val telefonoValido =
        telefono.count { it.isDigit() } >= 9

    val direccionValida =
        modalidad == "Retiro" ||
                direccion.trim().length >= 10

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
            Text("← Volver al carrito")
        }

        Text(
            text = "Confirmar pedido",
            style = MaterialTheme.typography.headlineMedium
        )

        // RESUMEN DEL PEDIDO
        Text(
            text = "Resumen de compra",
            style = MaterialTheme.typography.titleLarge
        )

        productosPedido.forEach { producto ->

            val cantidad = cantidades[producto.id] ?: 0
            val subtotal = producto.precio * cantidad

            Text(
                text = "${producto.nombre} x $cantidad"
            )

            Text(
                text = "Subtotal: $$subtotal"
            )
        }

        HorizontalDivider()

        Text(
            text = "Total: $$total",
            style = MaterialTheme.typography.headlineSmall
        )

        // MODALIDAD DE ENTREGA
        Text(
            text = "Modalidad de entrega",
            style = MaterialTheme.typography.titleMedium
        )

        Row {

            Row(
                verticalAlignment =
                    androidx.compose.ui.Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = modalidad == "Retiro",
                    onClick = { modalidad = "Retiro" }
                )

                Text("Retiro")
            }

            Spacer(modifier = Modifier.width(20.dp))

            Row(
                verticalAlignment =
                    androidx.compose.ui.Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = modalidad == "Despacho",
                    onClick = { modalidad = "Despacho" }
                )

                Text("Despacho")
            }
        }

        // DATOS DEL CLIENTE
        Text(
            text = "Datos de contacto",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre completo") },
            modifier = Modifier.fillMaxWidth(),
            isError = mostrarErrores && !nombreValido,
            supportingText = {
                if (mostrarErrores && !nombreValido) {
                    Text("Ingresa al menos 3 caracteres")
                }
            },
            singleLine = true
        )

        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Teléfono de contacto") },
            modifier = Modifier.fillMaxWidth(),
            isError = mostrarErrores && !telefonoValido,
            supportingText = {
                if (mostrarErrores && !telefonoValido) {
                    Text("Ingresa un teléfono válido")
                }
            },
            singleLine = true
        )

        // Dirección solamente para despacho
        if (modalidad == "Despacho") {

            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección de entrega") },
                modifier = Modifier.fillMaxWidth(),
                isError = mostrarErrores && !direccionValida,
                supportingText = {
                    if (mostrarErrores && !direccionValida) {
                        Text("Ingresa al menos 10 caracteres")
                    }
                }
            )
        }

        // CONFIRMAR
        Button(
            onClick = {

                mostrarErrores = true

                if (
                    nombreValido &&
                    telefonoValido &&
                    direccionValida &&
                    productosPedido.isNotEmpty()
                ) {

                    onConfirmar(
                        nombre.trim(),
                        telefono.trim(),
                        modalidad,
                        direccion.trim()
                    )
                }
            },
            enabled = productosPedido.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirmar pedido")
        }
    }
}
