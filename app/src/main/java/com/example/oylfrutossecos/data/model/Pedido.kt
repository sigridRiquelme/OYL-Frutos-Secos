package com.example.oylfrutossecos.data.model

data class DetallePedido(
    val idProducto: Int,
    val nombre: String,
    val cantidad: Int,
    val precioUnitario: Int
)

data class Pedido(
    val numero: String,
    val fecha: Long,
    val clienteId: Int?,
    val nombreCliente: String,
    val telefono: String,
    val modalidad: String,
    val direccion: String,
    val productos: List<DetallePedido>,
    val total: Int,
    val estado: String = "Pendiente"
)