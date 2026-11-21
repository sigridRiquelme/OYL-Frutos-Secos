package com.example.oylfrutossecos.data.repository

import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*

class AutenticacionPerfilTest {
    private lateinit var servidor: ServidorLocal
    private lateinit var origen: AutenticacionRemota
    private val sesion = SesionRemota(1, "Cliente", RolRemoto.CLIENTE, "jwt-prueba", 900)
    private val perfil = """{"nombre":"Cliente","nombreUsuario":"cliente","correo":"cliente@example.test","telefono":"912345678","direccion":"Santiago"}"""
    @Before fun preparar() { servidor = ServidorLocal(); origen = AutenticacionRemota("127.0.0.1", servidor.puerto) }
    @After fun cerrar() { servidor.close() }
    @Test fun registroEnviaDatosYPerfilUsaToken() = runBlocking {
        servidor.responder("{}")
        origen.registrar(RegistroRemoto("Cliente", "cliente", "cliente@example.test", "912345678", null, "clave-solo-prueba"))
        val registro = servidor.recibir()
        assertEquals("/api/auth/registro", registro.ruta); assertEquals("POST", registro.metodo)
        assertEquals("cliente", JsonParser.parseString(registro.cuerpo).asJsonObject["nombreUsuario"].asString)
        servidor.responder(perfil)
        val datos = origen.consultarPerfil(sesion)
        assertEquals("Cliente", datos.nombre); assertEquals("cliente", datos.nombreUsuario)
        assertEquals("cliente@example.test", datos.correo); assertEquals("912345678", datos.telefono)
        assertEquals("Santiago", datos.direccion)
        val consulta = servidor.recibir()
        assertEquals("GET", consulta.metodo); assertEquals("Bearer jwt-prueba", consulta.token)
        servidor.responder(perfil)
        assertEquals(datos, origen.actualizarPerfil(sesion, datos))
        val edicion = servidor.recibir()
        assertEquals("PUT", edicion.metodo); assertEquals("/api/clientes/me", edicion.ruta)
        assertEquals("Bearer jwt-prueba", edicion.token)
        assertEquals("Santiago", JsonParser.parseString(edicion.cuerpo).asJsonObject["direccion"].asString)
    }
    @Test fun loginRechazaRespuestasIncompletasOInvalidas() {
        val valido = """{"token":"jwt","tipo":"Bearer","expiraEn":900,"cliente":{"id":1,"nombre":"Cliente","rol":"CLIENTE"}}"""
        for (json in listOf(
            valido.replace("\"jwt\"", "null"), valido.replace("\"jwt\"", "\" \""),
            valido.replace("\"Bearer\"", "\"Basic\""), valido.replace(":900", ":0"),
            valido.replace("\"id\":1", "\"id\":0"), valido.replace("\"Cliente\"", "null"),
            valido.replace("\"Cliente\"", "\" \""), valido.replace("\"CLIENTE\"", "null"),
            """{"token":"jwt","tipo":"Bearer","expiraEn":900,"cliente":null}"""
        )) {
            servidor.responder(json)
            assertThrows(IllegalStateException::class.java) { runBlocking { origen.iniciarSesion("cliente", "clave") } }
        }
    }
}
