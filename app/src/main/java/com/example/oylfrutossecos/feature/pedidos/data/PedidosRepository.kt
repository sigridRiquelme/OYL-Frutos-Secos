
package com.example.oylfrutossecos.feature.pedidos.data

import android.content.Context

import com.example.oylfrutossecos.data.local.DetallePedidoLocal
import com.example.oylfrutossecos.data.local.PedidoLocal
import com.example.oylfrutossecos.data.local.PedidoLocalStore

// Contrato de las operaciones de pedidos
interface PedidosRepository {

    fun cargar(
        context: Context
    ): List<PedidoLocal>

    fun registrar(
        context: Context,
        clienteId: Int?,
        nombreCliente: String,
        telefono: String,
        modalidad: String,
        direccion: String,
        productos: List<DetallePedidoLocal>
    ): PedidoLocal

    fun actualizarEstado(
        context: Context,
        numero: String,
        nuevoEstado: String
    ): List<PedidoLocal>
}

// Implementación que utiliza el almacenamiento actual
object PedidosRepositoryLocal : PedidosRepository {

    override fun cargar(
        context: Context
    ): List<PedidoLocal> {

        return PedidoLocalStore.obtenerPedidos(
            context.applicationContext
        )
    }

    override fun registrar(
        context: Context,
        clienteId: Int?,
        nombreCliente: String,
        telefono: String,
        modalidad: String,
        direccion: String,
        productos: List<DetallePedidoLocal>
    ): PedidoLocal {

        return PedidoLocalStore.registrarPedido(
            context = context.applicationContext,
            clienteId = clienteId,
            nombreCliente = nombreCliente,
            telefono = telefono,
            modalidad = modalidad,
            direccion = direccion,
            productos = productos
        )
    }

    override fun actualizarEstado(
        context: Context,
        numero: String,
        nuevoEstado: String
    ): List<PedidoLocal> {

        return PedidoLocalStore.actualizarEstado(
            context.applicationContext,
            numero,
            nuevoEstado
        )
    }
}
