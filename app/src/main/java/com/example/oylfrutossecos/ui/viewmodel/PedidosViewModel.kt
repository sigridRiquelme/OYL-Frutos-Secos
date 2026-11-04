package com.example.oylfrutossecos.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.oylfrutossecos.data.local.DetallePedidoLocal
import com.example.oylfrutossecos.data.local.PedidoLocal
import com.example.oylfrutossecos.data.local.PedidoLocalStore

// Centraliza las operaciones de pedidos locales; el backend es una etapa posterior.
class PedidosViewModel : ViewModel() {
    var pedidos by mutableStateOf<List<PedidoLocal>>(emptyList())
        private set

    fun cargar(context: Context) {
        pedidos = PedidoLocalStore.obtenerPedidos(context.applicationContext)
    }

    fun registrar(
        context: Context,
        clienteId: Int?,
        nombreCliente: String,
        telefono: String,
        modalidad: String,
        direccion: String,
        productos: List<DetallePedidoLocal>
    ): PedidoLocal {
        val pedido = PedidoLocalStore.registrarPedido(
            context = context.applicationContext,
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

    fun actualizarEstado(context: Context, numero: String, nuevoEstado: String) {
        pedidos = PedidoLocalStore.actualizarEstado(
            context.applicationContext,
            numero,
            nuevoEstado
        )
    }
}
