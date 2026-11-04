package com.example.oylfrutossecos.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.oylfrutossecos.ui.screens.FotoProductoOYL
import com.example.oylfrutossecos.ui.screens.ProductoCatalogo

// Una misma vista de fotografía real para catálogo, detalle y carrito.
// Si el administrador agrega una fotografía, tiene prioridad sobre la imagen referencial.
@Composable
fun ProductoImagen(producto: ProductoCatalogo, modifier: Modifier = Modifier) {
    FotoProductoOYL(
        nombre = producto.nombre,
        imagenUri = producto.imagenUri,
        modifier = modifier
    )
}
