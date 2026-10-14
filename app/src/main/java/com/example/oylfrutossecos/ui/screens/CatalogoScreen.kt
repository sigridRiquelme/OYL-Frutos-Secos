
package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable

// Representa un producto de nuestro catálogo
data class ProductoCatalogo(
    val id: Int,
    val nombre: String,
    val categoria: String,
    val precio: Int,
    val stock: Int,
    val descripcion: String = "Producto natural de OYL Frutos Secos.",
    val presentacion: String = "Bolsa de 250 g"
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

    // Filtramos los productos
    val productosFiltrados = productos.filter { producto ->

        val coincideNombre = producto.nombre.contains(
            busqueda,
            ignoreCase = true
        )

        val coincideCategoria =
            categoriaSeleccionada == "Todos" ||
                    producto.categoria == categoriaSeleccionada

        coincideNombre && coincideCategoria
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

        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            label = { Text("Buscar productos") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filtros por categoría
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

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de productos
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

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
                    }
                }
            }
        }
    }
}
