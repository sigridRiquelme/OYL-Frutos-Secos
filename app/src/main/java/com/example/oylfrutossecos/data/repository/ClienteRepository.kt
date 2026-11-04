package com.example.oylfrutossecos.data.repository

import com.example.oylfrutossecos.data.local.dao.ClienteDao
import com.example.oylfrutossecos.data.local.entity.ClienteEntity

class ClienteRepository(private val clienteDao: ClienteDao) {
    suspend fun registrarCliente(cliente: ClienteEntity): Long =
        clienteDao.registrarCliente(cliente)

    suspend fun buscarPorNombreUsuario(nombreUsuario: String): ClienteEntity? =
        clienteDao.buscarPorNombreUsuario(nombreUsuario)

    suspend fun buscarPorId(id: Int): ClienteEntity? = clienteDao.buscarPorId(id)

    suspend fun existeNombreUsuario(nombreUsuario: String): Boolean =
        clienteDao.existeNombreUsuario(nombreUsuario) > 0

    suspend fun actualizarDatosCliente(
        clienteId: Int,
        nombre: String,
        nombreUsuario: String,
        correo: String,
        telefono: String,
        direccion: String
    ): Boolean {
        return clienteDao.actualizarDatosCliente(
            clienteId, nombre, nombreUsuario, correo, telefono, direccion
        ) == 1
    }
}
