package com.example.oylfrutossecos.data.repository

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/** Servidor efímero en loopback: nunca usa puertos de los microservicios reales. */
internal class ServidorLocal : AutoCloseable {
    data class Peticion(val metodo: String, val ruta: String, val token: String?, val clave: String?, val cuerpo: String)
    private val respuestas = LinkedBlockingQueue<Pair<Int, String>>()
    private val peticiones = LinkedBlockingQueue<Peticion>()
    private val servidor = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
        createContext("/") { conexion ->
            conexion.use {
                peticiones.add(Peticion(it.requestMethod, it.requestURI.toString(), it.requestHeaders.getFirst("Authorization"),
                    it.requestHeaders.getFirst("Idempotency-Key"), it.requestBody.bufferedReader().readText()))
                val (codigo, texto) = respuestas.poll(5, TimeUnit.SECONDS) ?: (500 to "{}")
                val bytes = texto.toByteArray(Charsets.UTF_8)
                it.responseHeaders.set("Content-Type", "application/json; charset=utf-8")
                it.sendResponseHeaders(codigo, bytes.size.toLong())
                it.responseBody.write(bytes)
            }
        }
        start()
    }
    val puerto get() = servidor.address.port
    fun responder(cuerpo: String, codigo: Int = 200) { respuestas.add(codigo to cuerpo) }
    fun recibir(): Peticion = checkNotNull(peticiones.poll(5, TimeUnit.SECONDS)) { "No llegó la petición" }
    override fun close() { servidor.stop(0) }
}
