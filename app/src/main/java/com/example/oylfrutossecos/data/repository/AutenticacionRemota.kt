package com.example.oylfrutossecos.data.repository

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

enum class RolRemoto { CLIENTE, ADMINISTRADOR }

internal data class DatosPerfil(
    val nombre: String,
    val nombreUsuario: String,
    val correo: String,
    val telefono: String,
    val direccion: String?
)

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

    internal suspend fun consultarPerfil(sesion: SesionRemota): DatosPerfil =
        api.consultarPerfil("Bearer ${sesion.token}")

    internal suspend fun actualizarPerfil(sesion: SesionRemota, datos: DatosPerfil): DatosPerfil =
        api.actualizarPerfil("Bearer ${sesion.token}", datos)

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
        @GET("api/clientes/me")
        suspend fun consultarPerfil(@Header("Authorization") token: String): DatosPerfil

        @PUT("api/clientes/me")
        suspend fun actualizarPerfil(
            @Header("Authorization") token: String,
            @Body datos: DatosPerfil
        ): DatosPerfil

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
