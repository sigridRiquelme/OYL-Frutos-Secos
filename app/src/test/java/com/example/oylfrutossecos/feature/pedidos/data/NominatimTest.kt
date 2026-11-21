package com.example.oylfrutossecos.feature.pedidos.data

import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class NominatimTest {
    private lateinit var servidor: HttpServer
    private lateinit var api: NominatimApi
    private val solicitudes = AtomicInteger()
    private val consulta = AtomicReference<String>()
    private val agente = AtomicReference<String>()

    @Before
    fun iniciarServidor() {
        servidor = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        api = crearNominatimApi("http://127.0.0.1:${servidor.address.port}/")
        servidor.start()
    }

    @After
    fun detenerServidor() = servidor.stop(0)

    @Test
    fun despachoConDireccionYListaCerrada() {
        assertTrue(esDespachoConDireccion("DESPACHO", DireccionDemo.MONEDA.consulta))
        assertFalse(esDespachoConDireccion("RETIRO", DireccionDemo.MONEDA.consulta))
        assertFalse(esDespachoConDireccion("Despacho", "  "))
        assertEquals(DireccionDemo.MONEDA, DireccionDemo.desde(DireccionDemo.MONEDA.consulta))
        assertNull(DireccionDemo.desde(DireccionDemo.MONEDA.consulta + ", departamento 1"))
    }

    @Test
    fun consultaUnaVezConParametrosYGuardaLaRespuesta() = runBlocking {
        responder(200, """[{"lat":"-33.44","lon":"-70.65","display_name":"La Moneda, Santiago, Chile","addresstype":"building"}]""")
        var guardadas = 0
        val resultado = consultarUbicacion(api, DireccionDemo.MONEDA) { guardadas = it.size }
        assertEquals(
            UbicacionDespacho.Encontrada(-33.44, -70.65, "La Moneda, Santiago, Chile"),
            resultado
        )
        val parametros = consulta.get().split("&").associate {
            val (nombre, valor) = it.split("=", limit = 2)
            nombre to URLDecoder.decode(valor, "UTF-8")
        }
        assertEquals(DireccionDemo.MONEDA.consulta, parametros["q"])
        assertEquals("jsonv2", parametros["format"])
        assertEquals("cl", parametros["countrycodes"])
        assertEquals("1", parametros["limit"])
        assertTrue(agente.get().startsWith("OYL-Frutos-Secos/"))
        assertEquals(1, guardadas)
        assertEquals(1, solicitudes.get())
    }

    @Test
    fun cacheaRespuestaVacia() = runBlocking {
        responder(200, "[]")
        var guardado = false
        val resultado = consultarUbicacion(api, DireccionDemo.MONEDA) { guardado = it.isEmpty() }
        assertEquals(UbicacionDespacho.NoEncontrada, resultado)
        assertTrue(guardado)
    }

    @Test
    fun representaAmbiguedadYRechazaCoordenadasInvalidas() {
        val calle = CoincidenciaNominatim("-33.44", "-70.65", "Calle, Santiago, Chile", "road")
        assertEquals(UbicacionDespacho.Ambigua, interpretarUbicacion(listOf(calle)))
        assertEquals(UbicacionDespacho.Ambigua, interpretarUbicacion(listOf(calle, calle)))
        assertEquals(UbicacionDespacho.RespuestaInvalida, interpretarUbicacion(listOf(calle.copy(lat = "NaN"))))
    }

    @Test
    fun reportaErrorHttpSinReintentarNiCachear() = runBlocking {
        responder(503, "", retryAfter = "0")
        val resultado = consultarUbicacion(api, DireccionDemo.MONEDA) { fail("No debe cachear errores HTTP") }
        assertEquals(UbicacionDespacho.ErrorHttp(503), resultado)
        assertEquals(1, solicitudes.get())
    }

    @Test
    fun reportaFalloDeConexion() = runBlocking {
        val sinServidor = crearNominatimApi("http://127.0.0.1:1/")
        assertEquals(
            UbicacionDespacho.SinConexion,
            consultarUbicacion(sinServidor, DireccionDemo.MONEDA) { fail("No debe cachear fallos de red") }
        )
    }

    private fun responder(codigo: Int, cuerpo: String, retryAfter: String? = null) {
        servidor.createContext("/search") { intercambio ->
            solicitudes.incrementAndGet()
            consulta.set(intercambio.requestURI.rawQuery)
            agente.set(intercambio.requestHeaders.getFirst("User-Agent"))
            if (retryAfter != null) intercambio.responseHeaders.set("Retry-After", retryAfter)
            val datos = cuerpo.toByteArray(Charsets.UTF_8)
            intercambio.sendResponseHeaders(codigo, if (datos.isEmpty()) -1L else datos.size.toLong())
            intercambio.responseBody.use { it.write(datos) }
        }
    }
}
