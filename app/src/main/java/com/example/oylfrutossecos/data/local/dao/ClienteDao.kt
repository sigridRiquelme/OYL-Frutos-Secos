package com.example.oylfrutossecos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import com.example.oylfrutossecos.data.local.entity.ClienteEntity

@Dao
interface ClienteDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registrarCliente(cliente: ClienteEntity): Long

    @Query("SELECT * FROM clientes WHERE nombreUsuario = :nombreUsuario LIMIT 1")
    suspend fun buscarPorNombreUsuario(nombreUsuario: String): ClienteEntity?

    @Query("SELECT * FROM clientes WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): ClienteEntity?

    @Query("SELECT COUNT(*) FROM clientes WHERE nombreUsuario = :nombreUsuario COLLATE NOCASE")
    suspend fun existeNombreUsuario(nombreUsuario: String): Int

    // El alias ahora se puede editar; la entidad ya tiene un indice UNIQUE.
    @Query("""
        UPDATE clientes SET
            nombre = :nombre,
            nombreUsuario = :nombreUsuario,
            correo = :correo,
            telefono = :telefono,
            direccion = :direccion
        WHERE id = :clienteId
    """)
    suspend fun actualizarDatosCliente(
        clienteId: Int,
        nombre: String,
        nombreUsuario: String,
        correo: String,
        telefono: String,
        direccion: String
    ): Int
}
