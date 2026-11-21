package com.example.oylfrutossecos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.oylfrutossecos.data.repository.CatalogoRemoto
import com.example.oylfrutossecos.data.repository.ItemCompra
import com.example.oylfrutossecos.data.repository.ModalidadCompra
import com.example.oylfrutossecos.data.model.Pedido
import com.example.oylfrutossecos.data.repository.PedidosRemotos
import com.example.oylfrutossecos.data.repository.SesionRemota
import com.example.oylfrutossecos.data.repository.SolicitudCompra
import com.example.oylfrutossecos.feature.navegacion.domain.ReglasNavegacionDashboard
import com.example.oylfrutossecos.ui.components.AccesoRapidoCarritoOYL
import com.example.oylfrutossecos.ui.components.BarraNavegacionOYL
import com.example.oylfrutossecos.ui.viewmodel.SesionRemotaViewModel
import java.util.UUID
import kotlinx.coroutines.CancellationException

private enum class PantallaCompra(val ruta: String) {
    INICIO("Inicio"), CATALOGO("Catalogo"), DETALLE("Detalle"), MAPA("Mapa"), QR("QR"),
    CARRITO("Carrito"), CONFIRMACION("Confirmacion"), HISTORIAL("Pedidos"), PERFIL("Perfil")
}

@Composable
internal fun ComprasConBackend(
    host: String,
    sesion: SesionRemota,
    modelo: SesionRemotaViewModel,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val catalogo = remember(host) { CatalogoRemoto(host) }
    val pedidos = remember(host) { PedidosRemotos(host) }
    val carrito = modelo.carrito
    var pantalla by rememberSaveable { mutableStateOf(PantallaCompra.INICIO) }
    var productoId by rememberSaveable { mutableStateOf<Int?>(null) }
    var origenDetalle by rememberSaveable { mutableStateOf(PantallaCompra.CATALOGO) }
    var actualizados by remember { mutableStateOf<List<ProductoCatalogo>?>(null) }
    var historial by remember { mutableStateOf<List<Pedido>?>(null) }
    var errorCarga by remember { mutableStateOf<String?>(null) }
    var avisoCarrito by remember { mutableStateOf<String?>(null) }
    var intento by remember { mutableIntStateOf(0) }

    fun abrir(destino: PantallaCompra) {
        modelo.limpiarResultadoCompra()
        avisoCarrito = null
        errorCarga = null
        if (destino in setOf(PantallaCompra.CARRITO, PantallaCompra.CATALOGO, PantallaCompra.QR)) {
            actualizados = null
        }
        if (destino == PantallaCompra.HISTORIAL) historial = null
        if (pantalla == destino) intento++
        pantalla = destino
    }

    fun verProducto(producto: ProductoCatalogo) {
        productoId = producto.id
        origenDetalle = pantalla
        abrir(PantallaCompra.DETALLE)
    }

    LaunchedEffect(pantalla, intento, sesion) {
        try {
            when (pantalla) {
                PantallaCompra.INICIO, PantallaCompra.CATALOGO, PantallaCompra.QR,
                PantallaCompra.CARRITO -> actualizados = catalogo.cargar()
                PantallaCompra.DETALLE -> if (actualizados == null) actualizados = catalogo.cargar()
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
    BackHandler(enabled = bloqueado || pantalla != PantallaCompra.INICIO) {
        if (!bloqueado) {
            abrir(when (pantalla) {
                PantallaCompra.CONFIRMACION -> PantallaCompra.CARRITO
                PantallaCompra.DETALLE -> origenDetalle
                else -> PantallaCompra.INICIO
            })
        }
    }

    Column(modifier.fillMaxSize()) {
        if (!bloqueado && ReglasNavegacionDashboard.mostrarAccesoCarrito("Cliente", pantalla.ruta)) {
            AccesoRapidoCarritoOYL(carrito.cantidades.values.sum()) { abrir(PantallaCompra.CARRITO) }
        }
        Column(Modifier.weight(1f).fillMaxWidth()) {
            when {
                pedido != null -> PedidoExitoScreen(
                    numeroPedido = pedido.numero,
                    modalidad = pedido.modalidad.etiqueta,
                    total = pedido.total,
                    onVolverInicio = { abrir(PantallaCompra.INICIO) },
                    onVerPedidos = { abrir(PantallaCompra.HISTORIAL) },
                    estado = pedido.estado.etiqueta
                )
                pendiente != null -> Column(Modifier.padding(16.dp)) {
                    if (modelo.enviando) {
                        CircularProgressIndicator()
                        Text("Enviando pedido…")
                    } else {
                        Text(modelo.errorCompra.orEmpty())
                        Button(onClick = { modelo.comprar(pedidos) }) { Text("Reintentar pedido") }
                    }
                }
                pantalla == PantallaCompra.INICIO -> {
                    if (error != null) {
                        TextButton(onClick = { abrir(PantallaCompra.INICIO) }) { Text("Reintentar carga del catálogo") }
                    } else if (productosActuales == null) {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                    InicioDashboardOYL(
                        rol = "Cliente",
                        nombreCliente = modelo.nombreMostrado,
                        productosDestacados = productosActuales.orEmpty(),
                        onAbrirMapa = { abrir(PantallaCompra.MAPA) },
                        onAbrirQr = { abrir(PantallaCompra.QR) },
                        onAbrirCatalogo = { abrir(PantallaCompra.CATALOGO) },
                        onAbrirCarrito = { abrir(PantallaCompra.CARRITO) },
                        onVerMisPedidos = { abrir(PantallaCompra.HISTORIAL) },
                        onGestionProductos = {},
                        onGestionInventario = {},
                        productosStockBajo = 0,
                        productosAgotados = 0,
                        onVerAlertasStock = {},
                        pedidosPendientes = 0,
                        onVerPedidosPendientes = {},
                        totalAvisos = 0,
                        onAbrirNotificaciones = {}
                    )
                }
                pantalla == PantallaCompra.MAPA -> MapaScreen(Modifier.fillMaxSize())
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
                    onCerrarSesion = onCerrarSesion,
                    modifier = Modifier.weight(1f)
                )
                error != null -> Column(Modifier.padding(16.dp)) {
                    Text(error)
                    Button(onClick = {
                        errorCarga = null
                        intento++
                    }) { Text("Reintentar") }
                }
                pantalla == PantallaCompra.CATALOGO && productosActuales != null ->
                    CatalogoScreen(productosActuales, Modifier.fillMaxSize(), ::verProducto)
                pantalla == PantallaCompra.QR && productosActuales != null -> QRScreen(
                    productos = productosActuales,
                    estadosProductos = productosActuales.associate { it.id to it.activo },
                    rol = "Cliente",
                    onVerProducto = ::verProducto,
                    onAbrirInventario = {},
                    modifier = Modifier.fillMaxSize()
                )
                pantalla == PantallaCompra.DETALLE && productosActuales != null -> {
                    val producto = productosActuales.firstOrNull { it.id == productoId && it.activo }
                    if (producto == null) {
                        Text("Este producto ya no está disponible.", Modifier.padding(16.dp))
                        TextButton(onClick = { abrir(origenDetalle) }) { Text("Volver") }
                    } else {
                        DetalleProductoScreen(
                            producto = producto,
                            rol = "Cliente",
                            cantidadYaAgregada = carrito.cantidades[producto.id] ?: 0,
                            onVolver = { abrir(origenDetalle) },
                            onAgregar = { carrito.agregar(producto, it) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
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
                    ListaMisPedidosOYL(pedidosCliente, onVolver = { abrir(PantallaCompra.INICIO) })
                }
                else -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
        if (!bloqueado) {
            val rutaActual = if (pantalla == PantallaCompra.DETALLE) origenDetalle.ruta else pantalla.ruta
            BarraNavegacionOYL(rutaActual) { ruta ->
                abrir(PantallaCompra.entries.first { it.ruta == ruta })
            }
        }
    }
}
