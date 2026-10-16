
package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.oylfrutossecos.ui.validation.DatosNuevoProducto

@Composable
fun GestionProductosScreen(
    productos: List<ProductoCatalogo>,
    estadosProductos: Map<Int, Boolean>,
    onVolver: () -> Unit,
    onCrearProducto: (
        DatosNuevoProducto,
        (Boolean) -> Unit
    ) -> Unit,
    onEditarProducto: (
        Int,
        DatosNuevoProducto,
        (Boolean) -> Unit
    ) -> Unit,
    onCambiarEstado: (
        Int,
        Boolean,
        (Boolean) -> Unit
    ) -> Unit,
    modifier: Modifier = Modifier
) {

    var mostrandoFormulario by rememberSaveable {
        mutableStateOf(false)
    }

    var idProductoEditando by rememberSaveable {
        mutableStateOf<Int?>(null)
    }

    var busqueda by rememberSaveable {
        mutableStateOf("")
    }

    // Producto que queremos activar o desactivar
    var productoPorConfirmar by remember {
        mutableStateOf<ProductoCatalogo?>(null)
    }

    // Evitar pulsar botones durante el guardado
    var idProcesando by remember {
        mutableStateOf<Int?>(null)
    }

    var mensajeError by remember {
        mutableStateOf("")
    }

    val productoEnEdicion = idProductoEditando?.let { id ->
        productos.firstOrNull { it.id == id }
    }

    // Confirmar antes de cambiar el estado
    productoPorConfirmar?.let { producto ->

        val estaActivo =
            estadosProductos[producto.id] ?: true

        AlertDialog(
            onDismissRequest = {
                productoPorConfirmar = null
            },

            title = {
                Text(
                    if (estaActivo)
                        "Desactivar producto"
                    else
                        "Activar producto"
                )
            },

            text = {
                Text(
                    if (estaActivo)
                        "¿Deseas desactivar ${producto.nombre}? " +
                                "El producto conservará sus datos."
                    else
                        "¿Deseas activar ${producto.nombre}?"
                )
            },

            confirmButton = {
                Button(
                    onClick = {

                        productoPorConfirmar = null
                        idProcesando = producto.id
                        mensajeError = ""

                        onCambiarEstado(
                            producto.id,
                            !estaActivo
                        ) { exitoso ->

                            idProcesando = null

                            if (!exitoso) {
                                mensajeError =
                                    "No se pudo cambiar el estado."
                            }
                        }
                    }
                ) {
                    Text("Confirmar")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        productoPorConfirmar = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Mostrar formulario de creación o edición
    if (mostrandoFormulario) {

        if (
            idProductoEditando != null &&
            productoEnEdicion == null
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text("Producto no disponible.")

                TextButton(
                    onClick = {
                        mostrandoFormulario = false
                        idProductoEditando = null
                    }
                ) {
                    Text("Volver al listado")
                }
            }

        } else {

            FormularioProductoScreen(
                producto = productoEnEdicion,

                onVolver = {
                    mostrandoFormulario = false
                    idProductoEditando = null
                },

                onGuardar = { datos, onResultado ->

                    val id = idProductoEditando

                    if (id == null) {

                        onCrearProducto(
                            datos,
                            onResultado
                        )

                    } else {

                        onEditarProducto(
                            id,
                            datos,
                            onResultado
                        )
                    }
                },

                modifier = modifier
            )
        }

        return
    }

    // Buscar productos
    val productosFiltrados = productos.filter {
        it.nombre.contains(
            busqueda,
            ignoreCase = true
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),

        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        TextButton(onClick = onVolver) {
            Text("← Volver al inicio")
        }

        Text(
            text = "Gestión de Productos",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Productos registrados: ${productos.size}"
        )

        Button(
            onClick = {
                idProductoEditando = null
                mostrandoFormulario = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ Nuevo producto")
        }

        OutlinedTextField(
            value = busqueda,
            onValueChange = {
                busqueda = it
            },
            label = {
                Text("Buscar producto")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (mensajeError.isNotEmpty()) {
            Text(
                text = mensajeError,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (productosFiltrados.isEmpty()) {

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No se encontraron productos.")
            }

        } else {

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(
                    items = productosFiltrados,
                    key = { it.id }
                ) { producto ->

                    val estaActivo =
                        estadosProductos[producto.id] ?: true

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
                                "Categoría: ${producto.categoria}"
                            )

                            Text(
                                "Precio: $${producto.precio}"
                            )

                            Text(
                                "Stock: ${producto.stock}"
                            )

                            Text(
                                "Presentación: ${producto.presentacion}"
                            )

                            // NUEVO: Mostrar estado
                            Text(
                                text = if (estaActivo)
                                    "Estado: Activo"
                                else
                                    "Estado: Inactivo",

                                color = if (estaActivo)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error
                            )

                            HorizontalDivider()

                            // Editar producto
                            OutlinedButton(
                                onClick = {
                                    idProductoEditando = producto.id
                                    mostrandoFormulario = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Editar")
                            }

                            // NUEVO: Activar o desactivar
                            Button(
                                onClick = {
                                    productoPorConfirmar = producto
                                },

                                enabled = idProcesando == null,

                                modifier = Modifier.fillMaxWidth()
                            ) {

                                Text(
                                    when {
                                        idProcesando == producto.id ->
                                            "Guardando..."

                                        estaActivo ->
                                            "Desactivar"

                                        else ->
                                            "Activar"
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
