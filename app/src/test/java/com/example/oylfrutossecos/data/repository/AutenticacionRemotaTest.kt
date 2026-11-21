package com.example.oylfrutossecos.data.repository

import com.google.gson.JsonParser
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException

class AutenticacionRemotaTest {
    private lateinit var servidor: HttpServer
    private lateinit var autenticacion: AutenticacionRemota
    private val solicitud = AtomicReference<Pair<String, String>>()
    private var codigo = 200

    @Before
    fun preparar() {
        servidor = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        servidor.createContext("/api/auth/login") { intercambio ->
            intercambio.use {
                val cuerpo = it.requestBody.bufferedReader().use { lector -> lector.readText() }
                solicitud.set(it.requestMethod to cuerpo)
                val respuesta = if (codigo == 200) {
                    """
                    {
                      "token": "jwt-de-prueba",
                      "tipo": "Bearer",
                      "expiraEn": 900,
                      "cliente": { "id": 15, "nombre": "Persona de prueba", "rol": "ADMINISTRADOR" }
                    }
                    """.trimIndent()
                } else {
                    "{}"
                }.toByteArray()
                it.responseHeaders.set("Content-Type", "application/json")
                it.sendResponseHeaders(codigo, respuesta.size.toLong())
                it.responseBody.write(respuesta)
            }
        }
        servidor.start()
        autenticacion = AutenticacionRemota("127.0.0.1", servidor.address.port)
    }

    @After
    fun cerrar() {
        servidor.stop(0)
    }

    @Test
    fun enviaCredencialesYLeeSesionDelBackend() = runBlocking {
        val sesion = autenticacion.iniciarSesion("persona_prueba", "clave de prueba")
        val (metodo, cuerpo) = solicitud.get()
        val json = JsonParser.parseString(cuerpo).asJsonObject
        assertEquals("POST", metodo)
        assertEquals("persona_prueba", json.get("nombreUsuario").asString)
        assertEquals("clave de prueba", json.get("contrasena").asString)
        assertEquals(15L, sesion.clienteId)
        assertEquals("Persona de prueba", sesion.nombre)
        assertEquals(RolRemoto.ADMINISTRADOR, sesion.rol)
        assertEquals("jwt-de-prueba", sesion.token)
        assertEquals(900L, sesion.expiraEnSegundos)
    }

    @Test
    fun propagaCredencialesRechazadas() {
        codigo = 401
        val error = org.junit.Assert.assertThrows(HttpException::class.java) {
            runBlocking { autenticacion.iniciarSesion("persona_prueba", "incorrecta") }
        }
        assertEquals(401, error.code())
    }
}
