package com.example.oylfrutossecos.data.model

data class ProductoCatalogo(
    val id: Int,
    val nombre: String,
    val categoria: String,
    val precio: Int,
    val stock: Int,
    val descripcion: String = "Producto natural de OYL Frutos Secos.",
    val presentacion: String = "Bolsa de 250 g",
    val imagenUri: String? = null,
    val activo: Boolean = true
)