
package com.example.oylfrutossecos.ui.screens

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Permite elegir una fotografía para el producto
@Composable
fun SelectorImagenProducto(
    imagenUri: String?,
    onImagenCambiada: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var errorImagen by remember {
        mutableStateOf("")
    }

    // Abrir selector de documentos de Android
    val selector = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->

        if (uri != null) {
            try {
                // Conservar permiso de lectura
                context.contentResolver
                    .takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                // Entregar la dirección de la fotografía
                onImagenCambiada(uri.toString())
                errorImagen = ""

            } catch (error: SecurityException) {
                errorImagen =
                    "No se pudo obtener acceso permanente a la imagen."
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Fotografía del producto",
            style = MaterialTheme.typography.titleMedium
        )

        ImagenProductoVistaPrevia(
            imagenUri = imagenUri,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        )

        Button(
            onClick = {
                selector.launch(arrayOf("image/*"))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (imagenUri == null)
                    "Seleccionar fotografía"
                else
                    "Cambiar fotografía"
            )
        }

        if (imagenUri != null) {
            TextButton(
                onClick = {
                    onImagenCambiada(null)
                }
            ) {
                Text("Quitar fotografía")
            }
        }

        if (errorImagen.isNotEmpty()) {
            Text(
                text = errorImagen,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}


// Muestra una fotografía desde su URI
@Composable
fun ImagenProductoVistaPrevia(
    imagenUri: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Cargar imagen fuera del hilo principal
    val imagen by produceState<ImageBitmap?>(
        initialValue = null,
        key1 = imagenUri
    ) {
        value = withContext(Dispatchers.IO) {

            if (imagenUri.isNullOrBlank()) {
                null
            } else {
                try {
                    val uri = Uri.parse(imagenUri)

                    // Consultar tamaño original
                    val limites = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }

                    context.contentResolver
                        .openInputStream(uri)?.use {
                            BitmapFactory.decodeStream(
                                it,
                                null,
                                limites
                            )
                        }

                    // Reducir imágenes muy grandes
                    var muestra = 1

                    while (
                        limites.outWidth / muestra > 720 ||
                        limites.outHeight / muestra > 720
                    ) {
                        muestra *= 2
                    }

                    val opciones = BitmapFactory.Options().apply {
                        inSampleSize = muestra
                    }

                    context.contentResolver
                        .openInputStream(uri)?.use {
                            BitmapFactory.decodeStream(
                                it,
                                null,
                                opciones
                            )
                        }?.asImageBitmap()

                } catch (error: Exception) {
                    null
                }
            }
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {

        if (imagen != null) {

            Image(
                bitmap = imagen!!,
                contentDescription = "Fotografía del producto",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

        } else {

            Text(
                text = if (imagenUri == null)
                    "Sin fotografía"
                else
                    "No se pudo mostrar la fotografía"
            )
        }
    }
}

