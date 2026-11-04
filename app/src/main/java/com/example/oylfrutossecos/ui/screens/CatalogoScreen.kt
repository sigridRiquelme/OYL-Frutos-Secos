package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale

// Mantener este modelo para que Dashboard, Carrito y Detalle sigan funcionando.
data class ProductoCatalogo(
    val id: Int,
    val nombre: String,
    val categoria: String,
    val precio: Int,
    val stock: Int,
    val descripcion: String = "Producto natural de OYL Frutos Secos.",
    val presentacion: String = "Bolsa de 250 g",
    val imagenUri: String? = null,
    val activo: Boolean = true
)

fun precioOYL(valor: Int): String = "$" + NumberFormat
    .getNumberInstance(Locale.forLanguageTag("es-CL")).format(valor)

@Composable
fun CatalogoScreen(
    productos: List<ProductoCatalogo>,
    modifier: Modifier = Modifier,
    onVerDetalle: (ProductoCatalogo) -> Unit = {}
) {
    var busqueda by rememberSaveable { mutableStateOf("") }
    var categoria by rememberSaveable { mutableStateOf("Todos") }
    val categorias = listOf("Todos", "Frutos secos", "Semillas", "Deshidratados")
    val filtrados = productos.filter {
        it.activo && it.nombre.contains(busqueda, ignoreCase = true) &&
                (categoria == "Todos" || it.categoria == categoria)
    }

    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 16.dp)) {
            Text("Nuestro catálogo", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            Text("Frutos secos, semillas y más", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                placeholder = { Text("Buscar productos…") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(15.dp)
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categorias.forEach { nombre ->
                    FilterChip(
                        selected = categoria == nombre,
                        onClick = { categoria = nombre },
                        label = { Text(nombre) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("${filtrados.size} productos", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filtrados.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Text("No encontramos productos con esos filtros.",
                        modifier = Modifier.padding(24.dp))
                }
            }
            items(filtrados, key = { it.id }) { producto ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onVerDetalle(producto) },
                    shape = RoundedCornerShape(17.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    FotoProductoOYL(
                        nombre = producto.nombre,
                        imagenUri = producto.imagenUri,
                        modifier = Modifier.fillMaxWidth().height(138.dp)
                    )
                    Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(producto.nombre, style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold, maxLines = 2,
                            overflow = TextOverflow.Ellipsis, minLines = 2)
                        Text(producto.presentacion, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(precioOYL(producto.precio),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleMedium)
                        Text(if (producto.stock > 0) "En stock · ${producto.stock} un." else "Agotado",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (producto.stock > 0) Color(0xFF286B4A)
                                    else MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
