
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
}
