
package com.example.oylfrutossecos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productos")
data class ProductoEntity(

    @PrimaryKey
    val id: Int,

    val nombre: String,

    val categoria: String,

    val precio: Int,

    val stock: Int,

    val descripcion: String,

    val presentacion: String,

    val imagenUri: String? = null
)
