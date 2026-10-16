
package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Producto utilizado por el catálogo y otras pantallas
data class ProductoCatalogo(
    val id: Int,
    val nombre: String,
    val categoria: String,
    val precio: Int,
    val stock: Int,
    val descripcion: String = "Producto natural de OYL Frutos Secos.",
    val presentacion: String = "Bolsa de 250 g",

    // Fotografía y estado recuperados desde Room
    val imagenUri: String? = null,
    val activo: Boolean = true
)

@Composable
fun CatalogoScreen(
    productos: List<ProductoCatalogo>,
    modifier: Modifier = Modifier,
    onVerDetalle: (ProductoCatalogo) -> Unit = {}
) {

    var busqueda by rememberSaveable {
        mutableStateOf("")
    }

    var categoriaSeleccionada by rememberSaveable {
        mutableStateOf("Todos")
    }

    val categorias = listOf(
        "Todos",
        "Frutos secos",
        "Semillas",
        "Deshidratados"
    )

    // Mostrar solamente productos activos.
    // Los desactivados siguen guardados en Room.
    val productosFiltrados = productos.filter { producto ->

        val coincideNombre = producto.nombre.contains(
            busqueda,
            ignoreCase = true
        )

        val coincideCategoria =
            categoriaSeleccionada == "Todos" ||
                    producto.categoria == categoriaSeleccionada

        producto.activo &&
                coincideNombre &&
                coincideCategoria
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Catálogo de productos",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // BUSCADOR
        OutlinedTextField(
            value = busqueda,

            onValueChange = {
                busqueda = it
            },

            label = {
                Text("Buscar productos")
            },

            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // FILTROS POR CATEGORÍA
        Row(
            modifier = Modifier.horizontalScroll(
                rememberScrollState()
            ),

            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            categorias.forEach { categoria ->

                FilterChip(
                    selected = categoriaSeleccionada == categoria,

                    onClick = {
                        categoriaSeleccionada = categoria
                    },

                    label = {
                        Text(categoria)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Productos encontrados: ${productosFiltrados.size}",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // LISTADO DE PRODUCTOS
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (productosFiltrados.isEmpty()) {

                item {

                    Text(
                        text = "No se encontraron productos disponibles.",
                        modifier = Modifier.padding(16.dp)
                    )
                }

            } else {

                items(
                    items = productosFiltrados,
                    key = { it.id }
                ) { producto ->

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onVerDetalle(producto)
                            }
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp),

                            verticalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            // FOTOGRAFÍA GUARDADA EN ROOM
                            if (!producto.imagenUri.isNullOrBlank()) {

                                ImagenProductoVistaPrevia(
                                    imagenUri = producto.imagenUri,

                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(170.dp)
                                )
                            }

                            Text(
                                text = producto.nombre,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = "Categoría: ${producto.categoria}"
                            )

                            Text(
                                text = "Presentación: ${producto.presentacion}"
                            )

                            Text(
                                text = "Precio: $${producto.precio}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = if (producto.stock == 0) {
                                    "Sin existencias"
                                } else {
                                    "Stock disponible: ${producto.stock}"
                                },

                                color = if (producto.stock == 0) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )

                            Text(
                                text = "Toca para ver el detalle",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
