
package com.example.oylfrutossecos.data.local

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

// Un producto incluido en una compra
data class DetallePedidoLocal(
    val idProducto: Int,
    val nombre: String,
    val cantidad: Int,
    val precioUnitario: Int
)

// Información de un pedido
data class PedidoLocal(
    val numero: String,
    val fecha: Long,
    val nombreCliente: String,
    val telefono: String,
    val modalidad: String,
    val direccion: String,
    val productos: List<DetallePedidoLocal>,
    val total: Int,
    val estado: String = "Pendiente"
)

// Almacenamiento privado del dispositivo
object PedidoLocalStore {

    private const val NOMBRE_ARCHIVO = "pedidos_oyl"
    private const val CLAVE_PEDIDOS = "lista_pedidos"
    private const val CLAVE_CONTADOR = "contador_pedidos"

    private fun preferencias(context: Context) =
        context.getSharedPreferences(
            NOMBRE_ARCHIVO,
            Context.MODE_PRIVATE
        )

    // Recuperar pedidos guardados
    fun obtenerPedidos(context: Context): List<PedidoLocal> {

        val texto = preferencias(context)
            .getString(CLAVE_PEDIDOS, "[]") ?: "[]"

        val arreglo = JSONArray(texto)

        return (0 until arreglo.length()).map { i ->

            val json = arreglo.getJSONObject(i)
            val detalles = json.getJSONArray("productos")

            val productos = (0 until detalles.length()).map { j ->

                val item = detalles.getJSONObject(j)

                DetallePedidoLocal(
                    idProducto = item.getInt("idProducto"),
                    nombre = item.getString("nombre"),
                    cantidad = item.getInt("cantidad"),
                    precioUnitario = item.getInt("precioUnitario")
                )
            }

            PedidoLocal(
                numero = json.getString("numero"),
                fecha = json.getLong("fecha"),
                nombreCliente = json.getString("nombreCliente"),
                telefono = json.getString("telefono"),
                modalidad = json.getString("modalidad"),
                direccion = json.getString("direccion"),
                productos = productos,
                total = json.getInt("total"),
                estado = json.getString("estado")
            )
        }
    }

    // Registrar una compra nueva
    @Synchronized
    fun registrarPedido(
        context: Context,
        nombreCliente: String,
        telefono: String,
        modalidad: String,
        direccion: String,
        productos: List<DetallePedidoLocal>
    ): PedidoLocal {

        require(productos.isNotEmpty()) {
            "El pedido debe contener productos."
        }

        require(productos.all {
            it.cantidad > 0 && it.precioUnitario >= 0
        }) {
            "Las cantidades y los precios deben ser válidos."
        }

        require(modalidad == "Retiro" || modalidad == "Despacho") {
            "Modalidad de entrega no válida."
        }

        val prefs = preferencias(context)
        val siguienteNumero =
            prefs.getInt(CLAVE_CONTADOR, 0) + 1

        val totalCalculado = productos.sumOf {
            it.precioUnitario * it.cantidad
        }

        val pedido = PedidoLocal(
            numero = "OYL-P-" +
                    siguienteNumero.toString().padStart(4, '0'),
            fecha = System.currentTimeMillis(),
            nombreCliente = nombreCliente,
            telefono = telefono,
            modalidad = modalidad,
            direccion = direccion,
            productos = productos,
            total = totalCalculado
        )

        val pedidosActualizados =
            obtenerPedidos(context) + pedido

        val guardado = prefs.edit()
            .putString(
                CLAVE_PEDIDOS,
                convertirAJson(pedidosActualizados)
            )
            .putInt(CLAVE_CONTADOR, siguienteNumero)
            .commit()

        check(guardado) {
            "No fue posible guardar el pedido."
        }

        return pedido
    }

    // Cambiar el estado de un pedido
    @Synchronized
    fun actualizarEstado(
        context: Context,
        numero: String,
        nuevoEstado: String
    ): List<PedidoLocal> {

        val pedidos = obtenerPedidos(context)

        val pedidoActual = pedidos.firstOrNull {
            it.numero == numero
        } ?: throw IllegalArgumentException(
            "El pedido no existe."
        )

        // Transiciones permitidas
        val estadosPermitidos = when (pedidoActual.estado) {

            "Pendiente" ->
                setOf("En preparación", "Cancelado")

            "En preparación" ->
                if (pedidoActual.modalidad == "Retiro") {
                    setOf("Listo para retiro", "Cancelado")
                } else {
                    setOf("En despacho", "Cancelado")
                }

            "Listo para retiro", "En despacho" ->
                setOf("Entregado")

            else -> emptySet()
        }

        require(nuevoEstado in estadosPermitidos) {
            "El cambio de estado no está permitido."
        }

        val actualizados = pedidos.map { pedido ->

            if (pedido.numero == numero) {
                pedido.copy(estado = nuevoEstado)
            } else {
                pedido
            }
        }

        val guardado = preferencias(context).edit()
            .putString(
                CLAVE_PEDIDOS,
                convertirAJson(actualizados)
            )
            .commit()

        check(guardado) {
            "No fue posible actualizar el estado."
        }

        return actualizados
    }

    // Transformar pedidos a formato JSON
    private fun convertirAJson(
        pedidos: List<PedidoLocal>
    ): String {

        val arreglo = JSONArray()

        pedidos.forEach { pedido ->

            val productosJson = JSONArray()

            pedido.productos.forEach { producto ->

                productosJson.put(
                    JSONObject()
                        .put("idProducto", producto.idProducto)
                        .put("nombre", producto.nombre)
                        .put("cantidad", producto.cantidad)
                        .put("precioUnitario", producto.precioUnitario)
                )
            }

            arreglo.put(
                JSONObject()
                    .put("numero", pedido.numero)
                    .put("fecha", pedido.fecha)
                    .put("nombreCliente", pedido.nombreCliente)
                    .put("telefono", pedido.telefono)
                    .put("modalidad", pedido.modalidad)
                    .put("direccion", pedido.direccion)
                    .put("productos", productosJson)
                    .put("total", pedido.total)
                    .put("estado", pedido.estado)
            )
        }

        return arreglo.toString()
    }
}
