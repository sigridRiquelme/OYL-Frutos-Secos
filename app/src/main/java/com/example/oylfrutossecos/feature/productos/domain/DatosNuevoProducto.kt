
package com.example.oylfrutossecos.feature.productos.domain

data class DatosNuevoProducto(
    val nombre: String,
    val categoria: String,
    val precio: String,
    val stock: String,
    val descripcion: String,
    val presentacion: String,
    val imagenUri: String? = null
)
