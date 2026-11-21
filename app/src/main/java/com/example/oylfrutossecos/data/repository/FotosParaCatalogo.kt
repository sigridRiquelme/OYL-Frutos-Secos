package com.example.oylfrutossecos.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

internal class FotosParaCatalogo(private val contexto: Context) {
    suspend fun preparar(referencia: String?): ByteArray? = withContext(Dispatchers.IO) {
        val uri = referencia?.let(Uri::parse) ?: return@withContext null
        if (uri.scheme != "content" && uri.scheme != "file") return@withContext null

        val foto = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(contexto.contentResolver, uri)) { decoder, info, _ ->
                val mayor = maxOf(info.size.width, info.size.height)
                val escala = minOf(1.0, LADO_MAXIMO.toDouble() / mayor)
                decoder.setTargetSize(
                    (info.size.width * escala).toInt().coerceAtLeast(1),
                    (info.size.height * escala).toInt().coerceAtLeast(1)
                )
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            leerCompatible(uri)
        }

        try {
            ByteArrayOutputStream().use { salida ->
                check(foto.compress(Bitmap.CompressFormat.JPEG, 88, salida))
                salida.toByteArray()
            }
        } finally {
            foto.recycle()
        }
    }

    private fun leerCompatible(uri: Uri): Bitmap {
        val resolver = contexto.contentResolver
        val opciones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opciones) }
        var muestra = 1
        while (maxOf(opciones.outWidth, opciones.outHeight) / muestra > LADO_MAXIMO) muestra *= 2
        val foto = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = muestra })
        } ?: error("No se pudo leer la fotografía")
        val orientacion = resolver.openInputStream(uri)?.use { entrada ->
            runCatching { ExifInterface(entrada).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1) }
                .getOrDefault(1)
        }
        val matriz = Matrix()
        when (orientacion) {
            2 -> matriz.setScale(-1f, 1f)
            3 -> matriz.setRotate(180f)
            4 -> matriz.setScale(1f, -1f)
            5 -> { matriz.setRotate(90f); matriz.postScale(-1f, 1f) }
            6 -> matriz.setRotate(90f)
            7 -> { matriz.setRotate(-90f); matriz.postScale(-1f, 1f) }
            8 -> matriz.setRotate(-90f)
        }
        if (matriz.isIdentity) return foto
        return try {
            Bitmap.createBitmap(foto, 0, 0, foto.width, foto.height, matriz, true)
        } finally {
            foto.recycle()
        }
    }

    private companion object {
        const val LADO_MAXIMO = 1600
    }
}
