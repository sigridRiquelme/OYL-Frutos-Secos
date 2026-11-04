package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.oylfrutossecos.ui.components.ProductoImagen

@Composable
fun DetalleProductoScreen(
    producto: ProductoCatalogo,
    rol: String,
    cantidadYaAgregada: Int,
    onVolver: () -> Unit,
    onAgregar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val disponible = (producto.stock - cantidadYaAgregada).coerceAtLeast(0)
    var cantidad by remember(producto.id, cantidadYaAgregada) {
        mutableIntStateOf(if (disponible > 0) 1 else 0)
    }
    var confirmado by remember(producto.id) { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onVolver) { Text("‹ Volver al catalogo") }
        Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
            ProductoImagen(producto, Modifier.fillMaxWidth().height(260.dp))
        }
        Text(producto.categoria.uppercase(), style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary)
        Text(producto.nombre, style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold)
        Text(producto.presentacion, style = MaterialTheme.typography.bodyMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(precioOYL(producto.precio), style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Surface(
                color = if (disponible > 0) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (disponible > 0) "Stock: $disponible" else "Sin stock",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelMedium)
            }
        }
        HorizontalDivider()
        Text("Descripcion", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold)
        Text(producto.descripcion, style = MaterialTheme.typography.bodyMedium)

        if (rol == "Cliente") {
            Text("Seleccionar cantidad", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(onClick = { if (cantidad > 1) cantidad-- },
                    enabled = cantidad > 1) { Text("−") }
                Text("$cantidad", style = MaterialTheme.typography.titleLarge)
                OutlinedButton(onClick = { if (cantidad < disponible) cantidad++ },
                    enabled = cantidad < disponible) { Text("+") }
            }
            Button(
                onClick = { onAgregar(cantidad); confirmado = true },
                enabled = cantidad > 0 && cantidad <= disponible,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Agregar al carrito") }
            if (confirmado) {
                Text("Producto agregado al carrito.",
                    color = MaterialTheme.colorScheme.primary)
            }
            if (cantidadYaAgregada > 0) {
                Text("En tu carrito: $cantidadYaAgregada unidades")
            }
        } else {
            Text("Puedes editar este articulo desde Gestion de productos.")
        }
    }
}
