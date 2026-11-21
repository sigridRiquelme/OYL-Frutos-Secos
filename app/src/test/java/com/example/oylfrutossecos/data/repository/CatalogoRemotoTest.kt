package com.example.oylfrutossecos.data.repository

import com.example.oylfrutossecos.feature.productos.domain.DatosNuevoProducto
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import retrofit2.HttpException

class CatalogoRemotoTest {
    private lateinit var productos: ServidorLocal
    private lateinit var inventario: ServidorLocal
    private lateinit var catalogo: CatalogoRemoto
    private val sesion = SesionRemota(1, "Admin", RolRemoto.ADMINISTRADOR, "jwt-prueba", 900)
    private val ruta = "/api/productos/1/foto/" + "a".repeat(64)
    private fun producto(id: Int = 1, foto: String? = null) = """{"id":$id,"nombre":"Nueces","categoria":"Frutos secos","precio":2500,"descripcion":null,"presentacion":null,"imagenUri":${foto?.let { "\"$it\"" } ?: "null"},"activo":true}"""
    private fun datos(imagen: String? = null) = DatosNuevoProducto("Nueces", "Frutos secos", "2500", "8", "Descripción", "500 g", imagen)
    @Before fun preparar() {
        productos = ServidorLocal(); inventario = ServidorLocal()
        catalogo = CatalogoRemoto("127.0.0.1", productos.puerto, inventario.puerto)
    }
    @After fun cerrar() { productos.close(); inventario.close() }

    @Test fun consultaCombinaStockYResuelveFotosRelativas() = runBlocking {
        productos.responder("[${producto(foto = ruta)},${producto(2, "https://ejemplo.test/foto.jpg")}]")
        inventario.responder("""[{"productoId":1,"stockDisponible":8}]""")
        val resultado = catalogo.cargar()
        assertEquals(listOf(8, 0), resultado.map { it.stock })
        assertEquals("http://127.0.0.1:${productos.puerto}$ruta", resultado[0].imagenUri)
        assertEquals("https://ejemplo.test/foto.jpg", resultado[1].imagenUri)
        assertEquals("", resultado[0].descripcion); assertEquals("", resultado[0].presentacion)
        assertEquals("/api/productos", productos.recibir().ruta)
        assertEquals("/api/inventario", inventario.recibir().ruta)
    }

    @Test fun creaConFotoStockYClaveIdempotente() = runBlocking {
        productos.responder(producto()); inventario.responder("""{"productoId":1,"stockDisponible":8}""")
        val resultado = catalogo.crear(sesion, datos("content://galeria/1"), "misma-clave", byteArrayOf(65, 66))
        assertEquals(8, resultado.stock)
        val peticion = productos.recibir()
        assertEquals("POST", peticion.metodo); assertEquals("Bearer jwt-prueba", peticion.token)
        assertEquals("misma-clave", peticion.clave)
        assertTrue(peticion.cuerpo.contains("name=\"stock\"")); assertTrue(peticion.cuerpo.contains("\r\n8\r\n"))
        assertTrue(peticion.cuerpo.contains("filename=\"producto.jpg\""))
        assertTrue(peticion.cuerpo.contains("image/jpeg")); assertTrue(peticion.cuerpo.contains("AB"))
        assertFalse(peticion.cuerpo.contains("content://"))
        assertEquals("/api/inventario/1", inventario.recibir().ruta)
    }

    @Test fun editaSinFotoConservandoReferenciaRemotaYStock() = runBlocking {
        for (referencia in listOf(null, "file:///foto.jpg", "https://ejemplo.test/foto.jpg", "http://127.0.0.1:${productos.puerto}$ruta")) {
            productos.responder(producto())
            assertEquals(7, catalogo.editar(sesion, 1, datos(referencia), 7, null).stock)
            val peticion = productos.recibir()
            assertEquals("PUT", peticion.metodo); assertEquals("/api/productos/1", peticion.ruta)
            assertFalse(peticion.cuerpo.contains("name=\"stock\"")); assertFalse(peticion.cuerpo.contains("filename="))
            assertFalse(peticion.cuerpo.contains("file:///"))
            if (referencia?.startsWith("http") == true) assertTrue(peticion.cuerpo.contains(if (referencia.contains(ruta)) ruta else referencia))
        }
    }

    @Test fun cambiaEstadoYRealizaEntradasYSalidas() = runBlocking {
        productos.responder(producto())
        assertEquals(4, catalogo.cambiarEstado(sesion, 1, false, 4).stock)
        val estado = productos.recibir()
        assertEquals("PATCH", estado.metodo); assertEquals("{\"activo\":false}", estado.cuerpo)
        for (entrada in listOf(true, false)) {
            inventario.responder("""{"productoId":1,"stockDisponible":3}""")
            assertEquals(3, catalogo.ajustarStock(sesion, 1, 2, entrada))
            val ajuste = inventario.recibir()
            assertEquals("POST", ajuste.metodo); assertEquals("/api/inventario/1/ajustes", ajuste.ruta)
            assertEquals("Bearer jwt-prueba", ajuste.token)
            assertTrue(ajuste.cuerpo.contains(if (entrada) "ENTRADA" else "SALIDA"))
            assertTrue(ajuste.cuerpo.contains("\"cantidad\":2"))
        }
    }

    @Test fun propagaErroresHttpSinInventarExistencias() = runBlocking {
        for (codigo in listOf(400, 401, 403, 404, 409, 413, 415, 500, 503)) {
            productos.responder("{}", codigo)
            val error = assertThrows(HttpException::class.java) { runBlocking { catalogo.cargar() } }
            assertEquals(codigo, error.code())
        }
        productos.responder("[${producto()}]"); inventario.responder("{}", 503)
        assertThrows(HttpException::class.java) { runBlocking { catalogo.cargar() } }
        Unit
    }

    @Test fun conexionInterrumpidaNoDevuelveCatalogoFalso() {
        productos.close()
        assertThrows(java.io.IOException::class.java) { runBlocking { catalogo.cargar() } }
    }
}
