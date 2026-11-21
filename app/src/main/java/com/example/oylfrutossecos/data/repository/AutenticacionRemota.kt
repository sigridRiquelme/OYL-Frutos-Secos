package com.example.oylfrutossecos.data.repository

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

enum class RolRemoto { CLIENTE, ADMINISTRADOR }

class RegistroRemoto(
    val nombre: String,
    val nombreUsuario: String,
    val correo: String,
    val telefono: String,
    val direccion: String?,
    val contrasena: String
)

class SesionRemota(
    val clienteId: Long,
    val nombre: String,
    val rol: RolRemoto,
    val token: String,
    val expiraEnSegundos: Long
)

class AutenticacionRemota(host: String, puerto: Int = 8083) {
    private val api = Retrofit.Builder()
        .baseUrl("http://$host:$puerto/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AuthApi::class.java)

    suspend fun registrar(datos: RegistroRemoto) {
        api.registrar(datos)
    }

    suspend fun iniciarSesion(usuario: String, contrasena: String): SesionRemota {
        val respuesta = api.iniciarSesion(Credenciales(usuario, contrasena))
        val cliente = checkNotNull(respuesta.cliente)
        val nombre = checkNotNull(cliente.nombre).also { check(it.isNotBlank()) }
        val token = checkNotNull(respuesta.token).also { check(it.isNotBlank()) }
        check(respuesta.tipo == "Bearer" && respuesta.expiraEn > 0 && cliente.id > 0)
        return SesionRemota(
            cliente.id, nombre, checkNotNull(cliente.rol), token, respuesta.expiraEn
        )
    }

    private interface AuthApi {
        @POST("api/auth/registro")
        suspend fun registrar(@Body datos: RegistroRemoto)

        @POST("api/auth/login")
        suspend fun iniciarSesion(@Body credenciales: Credenciales): Respuesta
    }

    private class Credenciales(val nombreUsuario: String, val contrasena: String)
    private class Respuesta(
        val token: String?,
        val tipo: String?,
        val expiraEn: Long,
        val cliente: Cliente?
    )
    private class Cliente(val id: Long, val nombre: String?, val rol: RolRemoto?)
}
