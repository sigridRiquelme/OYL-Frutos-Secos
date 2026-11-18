
package com.example.oylfrutossecos.ui.viewmodel

import android.content.Context

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

import com.example.oylfrutossecos.data.local.DetallePedidoLocal
import com.example.oylfrutossecos.data.local.PedidoLocal

import com.example.oylfrutossecos.feature.pedidos.data.PedidosRepository
import com.example.oylfrutossecos.feature.pedidos.data.PedidosRepositoryLocal

class PedidosViewModel(
    private val repository: PedidosRepository
) : ViewModel() {

    // Constructor sin parámetros para conservar
    // el funcionamiento actual de DashboardScreen.
    constructor() : this(PedidosRepositoryLocal)

    // Estado observado por la interfaz
    var pedidos by mutableStateOf<List<PedidoLocal>>(
        emptyList()
    )
        private set

    // Recuperar pedidos existentes
    fun cargar(context: Context) {

        pedidos = repository.cargar(context)
    }

    // Registrar un pedido nuevo
    fun registrar(
        context: Context,
        clienteId: Int?,
        nombreCliente: String,
        telefono: String,
        modalidad: String,
        direccion: String,
        productos: List<DetallePedidoLocal>
    ): PedidoLocal {

        val pedido = repository.registrar(
            context = context,
            clienteId = clienteId,
            nombreCliente = nombreCliente,
            telefono = telefono,
            modalidad = modalidad,
            direccion = direccion,
            productos = productos
        )

        pedidos = pedidos + pedido

        return pedido
    }

    // Actualizar el estado de un pedido
    fun actualizarEstado(
        context: Context,
        numero: String,
        nuevoEstado: String
    ) {

        pedidos = repository.actualizarEstado(
            context = context,
            numero = numero,
            nuevoEstado = nuevoEstado
        )
    }
}
