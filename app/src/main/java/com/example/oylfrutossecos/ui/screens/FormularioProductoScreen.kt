package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.oylfrutossecos.ui.validation.DatosNuevoProducto
import com.example.oylfrutossecos.ui.validation.ValidadorProducto

@Composable
fun FormularioProductoScreen(
    onVolver: () -> Unit,
    onGuardar: (DatosNuevoProducto, (Boolean) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var categoria by rememberSaveable {
        mutableStateOf("Frutos secos")
    }
    var precio by rememberSaveable { mutableStateOf("") }
    var stock by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var presentacion by rememberSaveable { mutableStateOf("") }

    var errores by remember {
        mutableStateOf<Map<String, String>>(emptyMap())
    }

    var guardando by remember { mutableStateOf(false) }
    var errorGuardado by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onVolver) {
            Text("← Volver a productos")
        }

        Text(
            "Nuevo producto",
            style = MaterialTheme.typography.headlineMedium
        )

        CampoProducto(
            "Nombre", nombre,
            { nombre = it }, errores["nombre"]
        )

        Text("Categoría")

        Row(
            modifier = Modifier.horizontalScroll(
                rememberScrollState()
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Frutos secos",
                "Semillas",
                "Deshidratados"
            ).forEach { opcion ->
                FilterChip(
                    selected = categoria == opcion,
                    onClick = { categoria = opcion },
                    label = { Text(opcion) }
                )
            }
        }

        CampoProducto(
            "Precio", precio,
            { precio = it }, errores["precio"],
            KeyboardType.Number
        )

        CampoProducto(
            "Stock inicial", stock,
            { stock = it }, errores["stock"],
            KeyboardType.Number
        )

        CampoProducto(
            "Descripción", descripcion,
            { descripcion = it }, errores["descripcion"]
        )

        CampoProducto(
            "Presentación", presentacion,
            { presentacion = it }, errores["presentacion"]
        )

        if (errorGuardado.isNotEmpty()) {
            Text(
                text = errorGuardado,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = {
                val datos = DatosNuevoProducto(
                    nombre = nombre.trim(),
                    categoria = categoria,
                    precio = precio.trim(),
                    stock = stock.trim(),
                    descripcion = descripcion.trim(),
                    presentacion = presentacion.trim()
                )

                errores = ValidadorProducto.validar(datos)
                errorGuardado = ""

                if (errores.isEmpty()) {
                    guardando = true

                    onGuardar(datos) { exitoso ->
                        guardando = false

                        if (exitoso) {
                            onVolver()
                        } else {
                            errorGuardado =
                                "No se pudo guardar el producto."
                        }
                    }
                }
            },
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (guardando) "Guardando..."
                else "Guardar producto"
            )
        }
    }
}

@Composable
private fun CampoProducto(
    etiqueta: String,
    valor: String,
    onCambio: (String) -> Unit,
    error: String?,
    tipo: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(error)
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = tipo
        )
    )
}

