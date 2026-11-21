package com.example.oylfrutossecos.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [27], manifest = Config.NONE)
class FotosParaCatalogoTest {
    private val contexto: Context get() = RuntimeEnvironment.getApplication()
    @Test fun ignoraReferenciasRemotasYNulas() = runBlocking {
        val fotos = FotosParaCatalogo(contexto)
        for (uri in listOf(null, "https://ejemplo.test/foto.jpg", "ftp://ejemplo.test/foto", "")) {
            assertNull(fotos.preparar(uri))
        }
    }

    private suspend fun comprobarFoto(ancho: Int, alto: Int) {
        val archivo = File.createTempFile("oyl-foto", ".png", contexto.cacheDir)
        try {
            val original = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888)
            archivo.outputStream().use { assertTrue(original.compress(Bitmap.CompressFormat.PNG, 100, it)) }
            original.recycle()
            // Una URI de galería es independiente del formato de rutas Windows/Unix.
            val uri = Uri.parse("content://oyl.fotos.test/imagen")
            // Proveedor de solo streams: ImageDecoder usa su fallback real de Android.
            // Robolectric legacy no implementa nCreate(FileDescriptor).
            shadowOf(contexto.contentResolver).registerInputStreamSupplier(uri) { archivo.inputStream() }
            val resultado = checkNotNull(FotosParaCatalogo(contexto).preparar(uri.toString()))
            assertTrue(resultado.isNotEmpty())
            val decodificada = checkNotNull(BitmapFactory.decodeByteArray(resultado, 0, resultado.size))
            assertTrue(decodificada.width <= 1600); assertTrue(decodificada.height <= 1600)
            assertTrue(decodificada.width > 0 && decodificada.height > 0)
            assertEquals(ancho.toDouble() / alto, decodificada.width.toDouble() / decodificada.height, 0.02)
            decodificada.recycle()
        } finally { archivo.delete() }
    }
    @Test fun reduceFotoGrandeYConservaProporcionEnApi27() = runBlocking { comprobarFoto(3200, 800) }
    @Test fun conservaFotoPequenaEnApi27() = runBlocking { comprobarFoto(120, 80) }
    @Test @Config(sdk = [28])
    fun procesaConImageDecoderEnApi28() = runBlocking { comprobarFoto(200, 100) }
    @Test fun propagaArchivosInexistentesSinDevolverFotoVacia() {
        assertThrows(Exception::class.java) {
            runBlocking { FotosParaCatalogo(contexto).preparar(Uri.fromFile(File(contexto.cacheDir, "inexistente-oyl.png")).toString()) }
        }
    }
}
