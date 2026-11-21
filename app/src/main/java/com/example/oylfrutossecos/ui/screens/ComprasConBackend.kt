package com.example.oylfrutossecos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.oylfrutossecos.data.repository.CatalogoRemoto
import com.example.oylfrutossecos.data.repository.ItemCompra
import com.example.oylfrutossecos.data.repository.ModalidadCompra
import com.example.oylfrutossecos.data.repository.PedidoRemoto
import com.example.oylfrutossecos.data.repository.PedidosRemotos
import com.example.oylfrutossecos.data.repository.SesionRemota
import com.example.oylfrutossecos.data.repository.SolicitudCompra
import com.example.oylfrutossecos.ui.viewmodel.SesionRemotaViewModel
import java.util.UUID
import kotlinx.coroutines.CancellationException

private enum class PantallaCompra { CATALOGO, CARRITO, CONFIRMACION, HISTORIAL, PERFIL }

@Composable
internal fun ComprasConBackend(
    host: String,
    sesion: SesionRemota,
    modelo: SesionRemotaViewModel,
    modifier: Modifier = Modifier
) {
    val catalogo = remember(host) { CatalogoRemoto(host) }
    val pedidos = remember(host) { PedidosRemotos(host) }
    val carrito = modelo.carrito
    var pantalla by rememberSaveable { mutableStateOf(PantallaCompra.CATALOGO) }
    var actualizados by remember { mutableStateOf<List<ProductoCatalogo>?>(null) }
    var historial by remember { mutableStateOf<List<PedidoRemoto>?>(null) }
    var errorCarga by remember { mutableStateOf<String?>(null) }
    var avisoCarrito by remember { mutableStateOf<String?>(null) }
    var intento by remember { mutableIntStateOf(0) }

    fun abrir(destino: PantallaCompra) {
        modelo.limpiarResultadoCompra()
        avisoCarrito = null
        errorCarga = null
        if (destino == PantallaCompra.CARRITO) actualizados = null
        if (destino == PantallaCompra.HISTORIAL) historial = null
        if (pantalla == destino) intento++
        pantalla = destino
    }

    LaunchedEffect(pantalla, intento, sesion) {
        try {
            when (pantalla) {
                PantallaCompra.CARRITO -> actualizados = catalogo.cargar()
                PantallaCompra.HISTORIAL -> historial = pedidos.historial(sesion)
                else -> Unit
            }
        } catch (cancelada: CancellationException) {
            throw cancelada
        } catch (_: Exception) {
            errorCarga = "No pudimos cargar los datos del backend."
        }
    }

    val pendiente = modelo.solicitud
    val pedido = modelo.pedido
    val error = errorCarga
    val productosActuales = actualizados
    val pedidosCliente = historial
    val bloqueado = pendiente != null || pedido != null
    BackHandler(enabled = bloqueado || pantalla != PantallaCompra.CATALOGO) {
        if (!bloqueado) {
            abrir(if (pantalla == PantallaCompra.CONFIRMACION) PantallaCompra.CARRITO else PantallaCompra.CATALOGO)
        }
    }

    Column(modifier.fillMaxSize()) {
        if (!bloqueado) {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = { abrir(PantallaCompra.CATALOGO) }) { Text("Catálogo") }
                TextButton(onClick = { abrir(PantallaCompra.CARRITO) }) {
                    Text("Carrito (${carrito.cantidades.values.sum()})")
                }
                TextButton(onClick = { abrir(PantallaCompra.HISTORIAL) }) { Text("Mis pedidos") }
                TextButton(onClick = { abrir(PantallaCompra.PERFIL) }) { Text("Mi perfil") }
            }
        }
        when {
            pedido != null -> Column(Modifier.padding(16.dp)) {
                Text("Pedido confirmado", style = MaterialTheme.typography.headlineSmall)
                Text("Pedido ${pedido.numero}")
                Text("Total: ${precioOYL(pedido.total)}")
                Button(onClick = { abrir(PantallaCompra.HISTORIAL) }) { Text("Ver mis pedidos") }
                TextButton(onClick = { abrir(PantallaCompra.CATALOGO) }) { Text("Volver al catálogo") }
            }
            pendiente != null -> Column(Modifier.padding(16.dp)) {
                if (modelo.enviando) {
                    CircularProgressIndicator()
                    Text("Enviando pedido…")
                } else {
                    Text(modelo.errorCompra.orEmpty())
                    Button(onClick = { modelo.comprar(pedidos) }) { Text("Reintentar pedido") }
                }
            }
            pantalla == PantallaCompra.CATALOGO -> CatalogoConBackend(
                host = host,
                rol = "Cliente",
                modifier = Modifier.weight(1f),
                cantidadesCarrito = carrito.cantidades,
                agregarAlCarrito = { producto, cantidad -> carrito.agregar(producto, cantidad) }
            )
            pantalla == PantallaCompra.CONFIRMACION -> {
                modelo.errorCompra?.let { Text(it, Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.error) }
                ConfirmacionPedidoScreen(
                    productos = carrito.productos.values.toList(),
                    cantidades = carrito.cantidades,
                    onVolver = { abrir(PantallaCompra.CARRITO) },
                    onConfirmar = { nombre, telefono, modalidad, direccion ->
                        val despacho = modalidad == "Despacho"
                        modelo.comprar(
                            pedidos,
                            SolicitudCompra(
                                solicitudId = UUID.randomUUID().toString(),
                                nombreCliente = nombre,
                                telefono = telefono,
                                modalidad = if (despacho) ModalidadCompra.DESPACHO else ModalidadCompra.RETIRO,
                                direccion = direccion.takeIf { despacho },
                                productos = carrito.cantidades.map { (id, cantidad) -> ItemCompra(id, cantidad) }
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    nombreInicial = modelo.nombreMostrado
                )
            }
            pantalla == PantallaCompra.PERFIL -> PerfilConBackend(
                host = host,
                sesion = sesion,
                onPerfilActualizado = { modelo.actualizarNombre(sesion, it) },
                modifier = Modifier.weight(1f)
            )
            error != null -> Column(Modifier.padding(16.dp)) {
                Text(error)
                Button(onClick = {
                    errorCarga = null
                    intento++
                }) { Text("Reintentar") }
            }
            pantalla == PantallaCompra.CARRITO && productosActuales != null -> {
                avisoCarrito?.let { Text(it, Modifier.padding(horizontal = 16.dp)) }
                val porId = productosActuales.associateBy { it.id }
                CarritoScreen(
                    productos = carrito.productos.values.toList(),
                    productosActuales = productosActuales,
                    cantidades = carrito.cantidades,
                    onCambiarCantidad = { id, cantidad -> carrito.cambiarCantidad(porId[id], id, cantidad) },
                    onVolverCatalogo = { abrir(PantallaCompra.CATALOGO) },
                    onConfirmarPedido = {
                        val hayFaltantes = carrito.cantidades.any { (id, cantidad) ->
                            val producto = porId[id]
                            producto == null || !producto.activo || producto.stock < cantidad
                        }
                        val cambioPrecio = carrito.productos.any { (id, producto) ->
                            porId[id]?.precio != producto.precio
                        }
                        when {
                            hayFaltantes -> avisoCarrito = "Ajusta las cantidades o elimina los productos sin stock."
                            cambioPrecio -> {
                                carrito.productos.keys.toList().forEach { id ->
                                    porId[id]?.let { carrito.productos[id] = it }
                                }
                                avisoCarrito = "Actualizamos los precios. Revisa el nuevo total antes de continuar."
                            }
                            else -> abrir(PantallaCompra.CONFIRMACION)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            pantalla == PantallaCompra.HISTORIAL && pedidosCliente != null -> {
                LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(16.dp)) {
                    item { Text("Historial de pedidos", style = MaterialTheme.typography.headlineSmall) }
                    if (pedidosCliente.isEmpty()) item { Text("Todavía no tienes pedidos.") }
                    items(pedidosCliente, key = { it.numero }) { registro ->
                        Column(Modifier.padding(vertical = 8.dp)) {
                            Text("Pedido ${registro.numero}")
                            Text("${registro.estado.replace('_', ' ')} · ${precioOYL(registro.total)}")
                        }
                    }
                }
            }
            else -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
