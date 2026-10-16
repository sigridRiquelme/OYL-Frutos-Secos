
package com.example.oylfrutossecos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import com.example.oylfrutossecos.data.local.entity.ClienteEntity

@Dao
interface ClienteDao {

    // Registrar un nuevo cliente
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registrarCliente(
        cliente: ClienteEntity
    ): Long

    // Buscar un cliente por nombre de usuario
    @Query("""
        SELECT * FROM clientes
        WHERE nombreUsuario = :nombreUsuario
        LIMIT 1
    """)
    suspend fun buscarPorNombreUsuario(
        nombreUsuario: String
    ): ClienteEntity?

    // Buscar un cliente por su ID
    @Query("""
        SELECT * FROM clientes
        WHERE id = :id
        LIMIT 1
    """)
    suspend fun buscarPorId(
        id: Int
    ): ClienteEntity?

    // Comprobar si el nombre de usuario existe
    @Query("""
        SELECT COUNT(*) FROM clientes
        WHERE nombreUsuario = :nombreUsuario
    """)
    suspend fun existeNombreUsuario(
        nombreUsuario: String
    ): Int


    @Query("""
    UPDATE clientes
    SET nombre = :nombre,
        correo = :correo,
        telefono = :telefono,
        direccion = :direccion
    WHERE id = :clienteId
""")
    suspend fun actualizarDatosCliente(
        clienteId: Int,
        nombre: String,
        correo: String,
        telefono: String,
        direccion: String
    ): Int

}
