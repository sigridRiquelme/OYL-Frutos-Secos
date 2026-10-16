
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
    modifier: Modifier = Modifier
) {

    // Controla si mostramos el formulario o el listado
    var mostrandoFormulario by rememberSaveable {
        mutableStateOf(false)
    }

    // Mostrar formulario para crear un producto
    if (mostrandoFormulario) {

        FormularioProductoScreen(
            onVolver = {
                mostrandoFormulario = false
            },
            onGuardar = onCrearProducto,
            modifier = modifier
        )

        return
    }

    // Texto utilizado para buscar productos
    var busqueda by rememberSaveable {
        mutableStateOf("")
    }

    // Filtrar los productos por nombre
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

        // Volver al Dashboard
        TextButton(
            onClick = onVolver
        ) {
            Text("← Volver al inicio")
        }

        // Título principal
        Text(
            text = "Gestión de Productos",
            style = MaterialTheme.typography.headlineMedium
        )

        // Cantidad de productos registrados
        Text(
            text = "Productos registrados: ${productos.size}"
        )

        // NUEVO: Botón para abrir formulario
        Button(
            onClick = {
                mostrandoFormulario = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ Nuevo producto")
        }

        // Buscador de productos
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

        // Mostrar mensaje si no hay resultados
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

            // Listado de productos desde Room
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
                                Arrangement.spacedBy(6.dp)
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
                        }
                    }
                }
            }
        }
    }
}
