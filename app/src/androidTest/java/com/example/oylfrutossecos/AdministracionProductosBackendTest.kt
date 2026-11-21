package com.example.oylfrutossecos

import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import com.example.oylfrutossecos.data.repository.AutenticacionRemota
import com.example.oylfrutossecos.data.repository.CatalogoRemoto
import com.example.oylfrutossecos.data.repository.FotosParaCatalogo
import com.example.oylfrutossecos.feature.productos.domain.DatosNuevoProducto
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File
import java.util.UUID

class AdministracionProductosBackendTest {
    @Test
    fun creaConFotoYStockEditaYConsultaDesdeOtraSesion() = runBlocking {
        val argumentos = InstrumentationRegistry.getArguments()
        val host = argumentos.getString("oylBackendHost").orEmpty()
        val usuario = argumentos.getString("oylAdminUsuario").orEmpty()
        val clave = argumentos.getString("oylAdminClave").orEmpty()
        assumeTrue("Configura el backend aislado y su administrador", host.isNotBlank() &&
            usuario.isNotBlank() && clave.isNotBlank())

        val sesion = AutenticacionRemota(host).iniciarSesion(usuario, clave)
        val admin = CatalogoRemoto(host)
        val publico = CatalogoRemoto(host)
        val idempotencia = UUID.randomUUID().toString()
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val archivo = File.createTempFile("producto-prueba", ".png", contexto.cacheDir)
        val imagen = Bitmap.createBitmap(40, 40, Bitmap.Config.ARGB_8888)
        val datos = DatosNuevoProducto("Producto USB ${idempotencia.take(8)}", "Frutos secos",
            "1300", "7", "Prueba de catálogo", "250 g", android.net.Uri.fromFile(archivo).toString())

        try {
            archivo.outputStream().use {
                imagen.eraseColor(android.graphics.Color.RED)
                assertTrue(imagen.compress(Bitmap.CompressFormat.PNG, 100, it))
            }
            val fotos = FotosParaCatalogo(contexto)
            val foto = fotos.preparar(datos.imagenUri)
            assertNotNull(foto)
            val creado = admin.crear(sesion, datos, idempotencia, foto)
            try {
                assertEquals(7, creado.stock)
                assertEquals(creado.id, admin.crear(sesion, datos, idempotencia, foto).id)
                val publicado = publico.cargar().single { it.id == creado.id }
                assertEquals(creado.imagenUri, publicado.imagenUri)
                assertFotoJpeg(checkNotNull(publicado.imagenUri))

                val retenido = admin.editar(sesion, creado.id, datos.copy(imagenUri = creado.imagenUri), 7, null)
                assertEquals(creado.imagenUri, retenido.imagenUri)

                archivo.outputStream().use {
                    imagen.eraseColor(android.graphics.Color.BLUE)
                    assertTrue(imagen.compress(Bitmap.CompressFormat.PNG, 100, it))
                }
                val reemplazo = admin.editar(sesion, creado.id, datos, 7, fotos.preparar(datos.imagenUri))
                assertNotEquals(creado.imagenUri, reemplazo.imagenUri)
                assertFotoJpeg(checkNotNull(reemplazo.imagenUri))

                val sinFoto = admin.editar(sesion, creado.id, datos.copy(imagenUri = null), 7, null)
                assertNull(sinFoto.imagenUri)
                assertEquals(7, publico.cargar().single { it.id == creado.id }.stock)
            } finally {
                admin.cambiarEstado(sesion, creado.id, activo = false, stock = 7)
            }
        } finally {
            imagen.recycle()
            archivo.delete()
        }
        Unit
    }

    private fun assertFotoJpeg(url: String) {
        OkHttpClient().newCall(Request.Builder().url(url).build()).execute().use {
            assertEquals(200, it.code)
            assertEquals("image/jpeg", it.header("Content-Type"))
            assertTrue(checkNotNull(it.body).bytes().isNotEmpty())
        }
    }
}
