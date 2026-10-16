
package com.example.oylfrutossecos.data.repository

import com.example.oylfrutossecos.data.local.dao.ClienteDao
import com.example.oylfrutossecos.data.local.entity.ClienteEntity

class ClienteRepository(
    private val clienteDao: ClienteDao
) {

    // Registrar un cliente en SQLite
    suspend fun registrarCliente(
        cliente: ClienteEntity
    ): Long {
        return clienteDao.registrarCliente(cliente)
    }

    // Buscar un cliente por nombre de usuario
    suspend fun buscarPorNombreUsuario(
        nombreUsuario: String
    ): ClienteEntity? {
        return clienteDao.buscarPorNombreUsuario(
            nombreUsuario
        )
    }

    // Buscar un cliente por ID
    suspend fun buscarPorId(
        id: Int
    ): ClienteEntity? {
        return clienteDao.buscarPorId(id)
    }

    // Comprobar si un nombre de usuario existe
    suspend fun existeNombreUsuario(
        nombreUsuario: String
    ): Boolean {
        return clienteDao.existeNombreUsuario(
            nombreUsuario
        ) > 0
    }


    suspend fun actualizarDatosCliente(
        clienteId: Int,
        nombre: String,
        correo: String,
        telefono: String,
        direccion: String
    ): Boolean {

        val filasActualizadas = clienteDao.actualizarDatosCliente(
            clienteId = clienteId,
            nombre = nombre,
            correo = correo,
            telefono = telefono,
            direccion = direccion
        )

        return filasActualizadas == 1
    }

}
