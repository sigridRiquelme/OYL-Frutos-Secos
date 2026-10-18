
package com.example.oylfrutossecos.ui.screens

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import java.io.File
import java.io.IOException


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

    // Conservar información de la fotografía pendiente
    var uriPendiente by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var archivoPendiente by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    // OPCIÓN 1: Elegir fotografía de galería
    val selectorGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->

        if (uri != null) {

            try {

                context.contentResolver
                    .takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                onImagenCambiada(uri.toString())
                errorImagen = ""

            } catch (error: SecurityException) {

                errorImagen =
                    "No se pudo acceder a la fotografía."
            }
        }
    }

    // OPCIÓN 2: Cámara en resolución completa
    val selectorCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { fotoGuardada ->

        if (fotoGuardada && uriPendiente != null) {

            // La cámara ya guardó el archivo original
            onImagenCambiada(uriPendiente)
            errorImagen = ""

        } else {

            // Si se cancela, eliminar archivo vacío
            archivoPendiente?.let { ruta ->
                File(ruta).delete()
            }
        }

        uriPendiente = null
        archivoPendiente = null
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

        // NUEVO: Fotografía de resolución completa
        Button(
            onClick = {

                try {

                    // Crear carpeta privada
                    val carpeta = File(
                        context.filesDir,
                        "fotos_productos"
                    )

                    if (!carpeta.exists() && !carpeta.mkdirs()) {
                        throw IOException(
                            "No se pudo crear la carpeta"
                        )
                    }

                    // Crear archivo con nombre único
                    val archivo = File.createTempFile(
                        "producto_",
                        ".jpg",
                        carpeta
                    )

                    // Obtener URI segura mediante FileProvider
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        archivo
                    )

                    uriPendiente = uri.toString()
                    archivoPendiente = archivo.absolutePath

                    // Abrir cámara
                    selectorCamara.launch(uri)

                    errorImagen = ""

                } catch (error: Exception) {

                    archivoPendiente?.let {
                        File(it).delete()
                    }

                    archivoPendiente = null
                    uriPendiente = null

                    errorImagen =
                        "No se pudo abrir la cámara."
                }
            },

            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📷 Tomar fotografía")
        }

        // Mantener la selección de galería
        OutlinedButton(
            onClick = {
                selectorGaleria.launch(
                    arrayOf("image/*")
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Elegir de galería")
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


// Mostrar fotografía guardada
@Composable
fun ImagenProductoVistaPrevia(
    imagenUri: String?,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

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

                    // Consultar dimensiones
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

                    // Reducir solo la imagen mostrada
                    // No modifica la fotografía original
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
