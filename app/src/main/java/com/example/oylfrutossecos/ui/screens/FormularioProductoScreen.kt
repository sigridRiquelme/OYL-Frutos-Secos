
package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

import com.example.oylfrutossecos.ui.validation.DatosNuevoProducto
import com.example.oylfrutossecos.ui.validation.ValidadorProducto

@Composable
fun FormularioProductoScreen(
    onVolver: () -> Unit,
    onGuardar: (
        DatosNuevoProducto,
        (Boolean) -> Unit
    ) -> Unit,
    modifier: Modifier = Modifier,
    producto: ProductoCatalogo? = null,

    // NUEVO: Fotografía existente al editar
    imagenUriInicial: String? = null
) {

    val esEdicion = producto != null

    // Nombre
    var nombre by rememberSaveable(producto?.id) {
        mutableStateOf(producto?.nombre.orEmpty())
    }

    // Categoría
    var categoria by rememberSaveable(producto?.id) {
        mutableStateOf(
            producto?.categoria ?: "Frutos secos"
        )
    }

    // Precio
    var precio by rememberSaveable(producto?.id) {
        mutableStateOf(
            producto?.precio?.toString().orEmpty()
        )
    }

    // Stock
    var stock by rememberSaveable(producto?.id) {
        mutableStateOf(
            producto?.stock?.toString().orEmpty()
        )
    }

    // Descripción
    var descripcion by rememberSaveable(producto?.id) {
        mutableStateOf(
            producto?.descripcion.orEmpty()
        )
    }

    // Presentación
    var presentacion by rememberSaveable(producto?.id) {
        mutableStateOf(
            producto?.presentacion.orEmpty()
        )
    }

    // NUEVO: Fotografía del producto
    var imagenUri by rememberSaveable(producto?.id) {
        mutableStateOf(imagenUriInicial)
    }

    // Errores de validación
    var errores by remember(producto?.id) {
        mutableStateOf<Map<String, String>>(emptyMap())
    }

    var guardando by remember(producto?.id) {
        mutableStateOf(false)
    }

    var errorGuardado by remember(producto?.id) {
        mutableStateOf("")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),

        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // Volver al listado
        TextButton(
            onClick = onVolver
        ) {
            Text("← Volver a productos")
        }

        // Título
        Text(
            text = if (esEdicion) {
                "Editar producto"
            } else {
                "Nuevo producto"
            },
            style = MaterialTheme.typography.headlineMedium
        )

        if (esEdicion) {
            Text("Producto ID: ${producto?.id}")
        }

        // Nombre
        CampoProducto(
            etiqueta = "Nombre",
            valor = nombre,
            onCambio = { nombre = it },
            error = errores["nombre"]
        )

        // Categoría
        Text("Categoría")

        Row(
            modifier = Modifier.horizontalScroll(
                rememberScrollState()
            ),

            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            listOf(
                "Frutos secos",
                "Semillas",
                "Deshidratados"
            ).forEach { opcion ->

                FilterChip(
                    selected = categoria == opcion,
                    onClick = {
                        categoria = opcion
                    },
                    label = {
                        Text(opcion)
                    }
                )
            }
        }

        // Precio
        CampoProducto(
            etiqueta = "Precio",
            valor = precio,
            onCambio = { precio = it },
            error = errores["precio"],
            tipo = KeyboardType.Number
        )

        // Stock
        CampoProducto(
            etiqueta = "Stock",
            valor = stock,
            onCambio = { stock = it },
            error = errores["stock"],
            tipo = KeyboardType.Number
        )

        // Descripción
        CampoProducto(
            etiqueta = "Descripción",
            valor = descripcion,
            onCambio = { descripcion = it },
            error = errores["descripcion"]
        )

        // Presentación
        CampoProducto(
            etiqueta = "Presentación",
            valor = presentacion,
            onCambio = { presentacion = it },
            error = errores["presentacion"]
        )

        HorizontalDivider()

        // NUEVO: Selector de fotografías
        SelectorImagenProducto(
            imagenUri = imagenUri,

            onImagenCambiada = { nuevaImagen ->
                imagenUri = nuevaImagen
            },

            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()

        // Mostrar error si falla el guardado
        if (errorGuardado.isNotEmpty()) {

            Text(
                text = errorGuardado,
                color = MaterialTheme.colorScheme.error
            )
        }

        // Guardar producto
        Button(
            onClick = {

                val datos = DatosNuevoProducto(
                    nombre = nombre.trim(),
                    categoria = categoria,
                    precio = precio.trim(),
                    stock = stock.trim(),
                    descripcion = descripcion.trim(),
                    presentacion = presentacion.trim(),

                    // NUEVO: Incluir fotografía
                    imagenUri = imagenUri
                )

                // Validaciones separadas de la UI
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
                                "No se pudieron guardar los cambios."
                        }
                    }
                }
            },

            enabled = !guardando,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                when {
                    guardando -> "Guardando..."
                    esEdicion -> "Guardar cambios"
                    else -> "Crear producto"
                }
            )
        }
    }
}


// Componente reutilizable para los campos
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
        label = {
            Text(etiqueta)
        },
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
