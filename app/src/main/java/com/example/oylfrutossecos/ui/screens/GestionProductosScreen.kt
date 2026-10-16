
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
    onVolver: () -> Unit,
    onCrearProducto: (DatosNuevoProducto, (Boolean) -> Unit) -> Unit,
    onEditarProducto: (Int, DatosNuevoProducto, (Boolean) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {

    // Controlar qué pantalla mostramos
    var mostrandoFormulario by rememberSaveable {
        mutableStateOf(false)
    }

    // ID del producto que estamos editando
    // Si es null, significa que crearemos uno nuevo
    var idProductoEditando by rememberSaveable {
        mutableStateOf<Int?>(null)
    }

    var busqueda by rememberSaveable {
        mutableStateOf("")
    }

    // Buscar el producto seleccionado
    val productoEnEdicion = idProductoEditando?.let { id ->
        productos.firstOrNull { it.id == id }
    }

    // Mostrar formulario de creación o edición
    if (mostrandoFormulario) {

        if (idProductoEditando != null &&
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

    // Filtrar productos por nombre
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

        // Regresar al Dashboard
        TextButton(
            onClick = onVolver
        ) {
            Text("← Volver al inicio")
        }

        Text(
            text = "Gestión de Productos",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Productos registrados: ${productos.size}"
        )

        // Crear nuevo producto
        Button(
            onClick = {
                idProductoEditando = null
                mostrandoFormulario = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ Nuevo producto")
        }

        // Buscador
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

        // Listado de productos
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
                                text = "Categoría: ${producto.categoria}"
                            )

                            Text(
                                text = "Precio: $${producto.precio}"
                            )

                            Text(
                                text = "Stock: ${producto.stock}"
                            )

                            Text(
                                text = "Presentación: ${producto.presentacion}"
                            )

                            HorizontalDivider()

                            // NUEVO: Botón para editar
                            OutlinedButton(
                                onClick = {
                                    idProductoEditando = producto.id
                                    mostrandoFormulario = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Editar")
                            }
                        }
                    }
                }
            }
        }
    }
}
