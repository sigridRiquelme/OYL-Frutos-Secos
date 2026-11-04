package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import coil3.compose.AsyncImage
import java.util.Locale

// Fotos reales de Pexels; se muestran solo cuando el producto no tiene imagen propia.
// Las imagenes son referenciales: NO son fotografias de productos oficiales de OYL.
private const val HOST_FOTO = "https://images.pexels.com/photos/"

fun fotoReferencialOYL(nombre: String): String {
    val normalizado = nombre.lowercase(Locale.ROOT)
    // Cranberries deshidratados reales, foto CC0 de Wikimedia Commons.
    if ("cranberr" in normalizado) {
        return "https://upload.wikimedia.org/wikipedia/commons/3/32/Dried_cranberries_%282%29.jpg"
    }
    val id = when {
        "almendra" in normalizado -> "4753649"
        "nuec" in normalizado -> "4499239"
        "chia" in normalizado || "chía" in normalizado -> "27468397"
        "pasa" in normalizado -> "4499228"
        "maravilla" in normalizado || "girasol" in normalizado -> "5254830"
        else -> "4753637" // Mezcla real de frutos secos para productos de demostracion.
    }
    return "${HOST_FOTO}${id}/pexels-photo-${id}.jpeg?auto=compress&cs=tinysrgb&w=900"
}

@Composable
fun FotoProductoOYL(
    nombre: String,
    imagenUri: String?,
    modifier: Modifier = Modifier
) {
    val url = if (imagenUri.isNullOrBlank()) fotoReferencialOYL(nombre) else imagenUri
    var error by remember(url) { mutableStateOf(false) }
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (error) {
            Text(
                text = "Foto no disponible sin conexión",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        } else {
            AsyncImage(
                model = url,
                contentDescription = "Fotografia de $nombre",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onError = { error = true }
            )
        }
    }
}
