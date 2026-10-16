
package com.example.oylfrutossecos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "clientes",
    indices = [
        Index(value = ["nombreUsuario"], unique = true)
    ]
)
data class ClienteEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nombre: String,

    val nombreUsuario: String,

    val correo: String,

    val telefono: String,

    val direccion: String,

    val passwordHash: String,

    val passwordSalt: String
)
