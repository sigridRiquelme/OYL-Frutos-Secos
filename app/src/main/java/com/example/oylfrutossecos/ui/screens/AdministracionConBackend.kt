package com.example.oylfrutossecos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.oylfrutossecos.data.model.Pedido
import com.example.oylfrutossecos.data.repository.CatalogoRemoto
import com.example.oylfrutossecos.data.repository.FotosParaCatalogo
import com.example.oylfrutossecos.data.repository.EstadoPedidoRemoto
import com.example.oylfrutossecos.data.repository.PedidosRemotos
import com.example.oylfrutossecos.data.repository.SesionRemota
import com.example.oylfrutossecos.feature.administracion.domain.CalculadoraResumenAdministrativo
import com.example.oylfrutossecos.ui.components.BarraNavegacionOYL
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import retrofit2.HttpException

private enum class AreaAdministrativa {
    PRODUCTOS, INVENTARIO, PEDIDOS, MAPA, QR, PERFIL, NOTIFICACIONES
}

@Composable
internal fun AdministracionConBackend(
    host: String,
    sesion: SesionRemota,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = androidx.compose.ui.platform.LocalContext.current
    val catalogo = remember(host) { CatalogoRemoto(host) }
    val fotos = remember(contexto) { FotosParaCatalogo(contexto.applicationContext) }
    val pedidosRemotos = remember(host) { PedidosRemotos(host) }
    val alcance = rememberCoroutineScope()
    var area by rememberSaveable { mutableStateOf<AreaAdministrativa?>(null) }
    var productos by remember { mutableStateOf<List<ProductoCatalogo>?>(null) }
    var pedidos by remember { mutableStateOf<List<Pedido>?>(null) }
    var errorCarga by remember { mutableStateOf<String?>(null) }
    var aviso by remember { mutableStateOf<String?>(null) }
    var recarga by remember { mutableIntStateOf(0) }
    var guardando by remember { mutableStateOf(false) }
    var productoInventario by rememberSaveable { mutableStateOf<Int?>(null) }
    var filtroInventario by rememberSaveable { mutableStateOf("Todos") }
    var filtroPedidos by rememberSaveable { mutableStateOf("Todos") }

    fun abrir(
        destino: AreaAdministrativa?,
        productoId: Int? = null,
        filtro: String = "Todos"
    ) {
        if (guardando) return
        productos = null
        pedidos = null
        errorCarga = null
        aviso = null
        productoInventario = productoId
        filtroInventario = filtro
        filtroPedidos = filtro
        if (area == destino) recarga++
        area = destino
    }

    fun actualizarProducto(producto: ProductoCatalogo) {
        productos = productos?.map { if (it.id == producto.id) producto else it }
    }

    fun guardar(resultado: (String?) -> Unit, conservarFormulario: Boolean = false,
                operacion: suspend () -> Unit) {
        if (guardando) {
            resultado("Espera a que termine la operación en curso.")
            return
        }
        guardando = true
        aviso = null
        alcance.launch {
            try {
                operacion()
                resultado(null)
            } catch (cancelada: CancellationException) {
                throw cancelada
            } catch (fallo: Exception) {
                val mensajePorDefecto = if (conservarFormulario) {
                    "No pudimos confirmar el cambio. Vuelve a guardar desde este formulario con los mismos datos."
                } else {
                    "No pudimos confirmar el cambio. Recarga antes de repetir la operación."
                }
                val mensaje = mensajeAdministracion(fallo, mensajePorDefecto)
                aviso = mensaje
                resultado(mensaje)
                val codigo = (fallo as? HttpException)?.code()
                if (!conservarFormulario && (codigo == null || codigo >= 500)) {
                    productos = null
                    pedidos = null
                    errorCarga = mensaje
                    aviso = null
                }
            } finally {
                guardando = false
            }
        }
    }

    LaunchedEffect(area, recarga, sesion) {
        try {
            when (area) {
                AreaAdministrativa.PRODUCTOS, AreaAdministrativa.INVENTARIO,
                AreaAdministrativa.QR -> productos = catalogo.cargar()
                AreaAdministrativa.PEDIDOS -> pedidos = pedidosRemotos.listarParaAdministracion(sesion)
                null, AreaAdministrativa.PERFIL, AreaAdministrativa.NOTIFICACIONES -> {
                    productos = catalogo.cargar()
                    pedidos = pedidosRemotos.listarParaAdministracion(sesion)
                }
                AreaAdministrativa.MAPA -> Unit
            }
        } catch (cancelada: CancellationException) {
            throw cancelada
        } catch (fallo: Exception) {
            errorCarga = mensajeAdministracion(fallo, "No pudimos cargar los datos del backend.")
        }
    }

    val destino = area
    val listaProductos = productos
    val listaPedidos = pedidos
    val resumen = CalculadoraResumenAdministrativo.calcular(listaProductos.orEmpty(), listaPedidos.orEmpty())
    val fallo = errorCarga
    BackHandler(enabled = destino != null) { abrir(null) }
    Column(modifier.fillMaxSize()) {
        if (guardando) LinearProgressIndicator(Modifier.fillMaxWidth())
        aviso?.let { Text(it, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.error) }
        Column(Modifier.weight(1f).fillMaxWidth()) {
            when {
                destino == null -> {
                    if (fallo != null) {
                        TextButton(onClick = { abrir(null) }) { Text("Reintentar carga del resumen") }
                    } else if (listaPedidos == null || listaProductos == null) {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                    InicioDashboardOYL(
                        rol = "Administrador",
                        nombreCliente = sesion.nombre,
                        productosDestacados = emptyList(),
                        onAbrirMapa = { abrir(AreaAdministrativa.MAPA) },
                        onAbrirQr = { abrir(AreaAdministrativa.QR) },
                        onAbrirCatalogo = {},
                        onAbrirCarrito = {},
                        onVerMisPedidos = { abrir(AreaAdministrativa.PEDIDOS) },
                        onGestionProductos = { abrir(AreaAdministrativa.PRODUCTOS) },
                        onGestionInventario = { abrir(AreaAdministrativa.INVENTARIO) },
                        productosStockBajo = resumen.productosStockBajo,
                        productosAgotados = resumen.productosAgotados,
                        onVerAlertasStock = { abrir(AreaAdministrativa.INVENTARIO, filtro = "Por reponer") },
                        pedidosPendientes = resumen.pedidosPendientes,
                        onVerPedidosPendientes = { abrir(AreaAdministrativa.PEDIDOS, filtro = "Pendiente") },
                        totalAvisos = resumen.totalAvisos,
                        onAbrirNotificaciones = { abrir(AreaAdministrativa.NOTIFICACIONES) }
                    )
                }
                destino == AreaAdministrativa.PERFIL -> {
                    if (fallo != null) {
                        TextButton(onClick = { abrir(destino) }) { Text("Reintentar carga del resumen") }
                    } else if (listaPedidos == null || listaProductos == null) {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                    PerfilAdministradorScreen(
                        productosActivos = resumen.productosActivos,
                        alertasStock = resumen.alertasStock,
                        pedidosPendientes = resumen.pedidosPendientes,
                        pedidosRegistrados = resumen.pedidosRegistrados,
                        onVolver = { abrir(null) },
                        onGestionProductos = { abrir(AreaAdministrativa.PRODUCTOS) },
                        onGestionInventario = { abrir(AreaAdministrativa.INVENTARIO) },
                        onGestionPedidos = { abrir(AreaAdministrativa.PEDIDOS) },
                        onNotificaciones = { abrir(AreaAdministrativa.NOTIFICACIONES) },
                        onCerrarSesion = onCerrarSesion
                    )
                }
                destino == AreaAdministrativa.MAPA -> MapaScreen(Modifier.fillMaxSize())
                fallo != null -> Column(Modifier.padding(20.dp)) {
                    Text(fallo, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { abrir(destino, productoInventario,
                        if (destino == AreaAdministrativa.PEDIDOS) filtroPedidos else filtroInventario
                    ) }) { Text("Recargar") }
                    TextButton(onClick = { abrir(null) }) { Text("Volver") }
                }
                destino == AreaAdministrativa.QR && listaProductos != null -> QRScreen(
                    productos = listaProductos,
                    estadosProductos = listaProductos.associate { it.id to it.activo },
                    rol = "Administrador",
                    onVerProducto = {},
                    onAbrirInventario = { abrir(AreaAdministrativa.INVENTARIO, productoId = it) }
                )
                destino == AreaAdministrativa.NOTIFICACIONES && listaProductos != null && listaPedidos != null ->
                    NotificacionesScreen(
                        productos = listaProductos,
                        estadosProductos = listaProductos.associate { it.id to it.activo },
                        pedidos = listaPedidos,
                        onVolver = { abrir(null) },
                        onAbrirInventario = { abrir(AreaAdministrativa.INVENTARIO, filtro = it) },
                        onAbrirPedidos = { abrir(AreaAdministrativa.PEDIDOS, filtro = "Pendiente") }
                    )
                destino == AreaAdministrativa.PRODUCTOS && listaProductos != null -> GestionProductosScreen(
                    productos = listaProductos,
                    estadosProductos = listaProductos.associate { it.id to it.activo },
                    imagenesProductos = listaProductos.associate { it.id to it.imagenUri },
                    onVolver = { abrir(null) },
                    onCrearProducto = { datos, clave, resultado ->
                        guardar({ resultado(it == null) }, conservarFormulario = true) {
                            val creado = catalogo.crear(sesion, datos, clave, fotos.preparar(datos.imagenUri))
                            productos = listaProductos.filterNot { it.id == creado.id } + creado
                        }
                    },
                    onEditarProducto = { id, datos, resultado ->
                        guardar({ resultado(it == null) }, conservarFormulario = true) {
                            val stock = listaProductos.first { it.id == id }.stock
                            actualizarProducto(catalogo.editar(sesion, id, datos, stock, fotos.preparar(datos.imagenUri)))
                        }
                    },
                    onCambiarEstado = { id, activo, resultado ->
                        guardar({ resultado(it == null) }) {
                            val stock = listaProductos.first { it.id == id }.stock
                            actualizarProducto(catalogo.cambiarEstado(sesion, id, activo, stock))
                        }
                    },
                    modifier = Modifier.weight(1f),
                    edicionRemota = true
                )
                destino == AreaAdministrativa.INVENTARIO && listaProductos != null -> GestionInventarioScreen(
                    productos = listaProductos,
                    estadosProductos = listaProductos.associate { it.id to it.activo },
                    onVolver = { abrir(null) },
                    onAjustarStock = { id, texto, entrada, resultado ->
                        val cantidad = texto.trim().toIntOrNull()
                        if (cantidad == null || cantidad <= 0) {
                            resultado("Ingresa una cantidad mayor a cero.")
                        } else guardar(resultado) {
                            val producto = listaProductos.first { it.id == id }
                            val stock = catalogo.ajustarStock(sesion, id, cantidad, entrada)
                            actualizarProducto(producto.copy(stock = stock))
                        }
                    },
                    modifier = Modifier.weight(1f),
                    productoInicialId = productoInventario,
                    filtroInicial = filtroInventario
                )
                destino == AreaAdministrativa.PEDIDOS && listaPedidos != null -> GestionPedidosScreen(
                    pedidos = listaPedidos,
                    onVolver = { abrir(null) },
                    onCambiarEstado = { numero, etiqueta, resultado ->
                        val estado = EstadoPedidoRemoto.entries.firstOrNull { it.etiqueta == etiqueta }
                        if (estado == null) {
                            resultado("El estado elegido no está disponible.")
                        } else guardar(resultado) {
                            val actualizado = pedidosRemotos.cambiarEstado(sesion, numero, estado)
                            pedidos = pedidos?.map { if (it.numero == numero) actualizado else it }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    filtroInicial = filtroPedidos
                )
                else -> CircularProgressIndicator(Modifier.padding(20.dp))
            }
        }
        val ruta = when (destino) {
            null -> "Inicio"
            AreaAdministrativa.MAPA -> "Mapa"
            AreaAdministrativa.QR -> "QR"
            AreaAdministrativa.PEDIDOS -> "Pedidos"
            AreaAdministrativa.PERFIL -> "Perfil"
            else -> ""
        }
        BarraNavegacionOYL(ruta) {
            abrir(when (it) {
                "Mapa" -> AreaAdministrativa.MAPA
                "QR" -> AreaAdministrativa.QR
                "Pedidos" -> AreaAdministrativa.PEDIDOS
                "Perfil" -> AreaAdministrativa.PERFIL
                else -> null
            })
        }
    }
}

private fun mensajeAdministracion(fallo: Exception, porDefecto: String): String =
    when ((fallo as? HttpException)?.code()) {
        400 -> "Revisa los datos de la operación."
        401, 403 -> "Tu sesión no tiene acceso a esta operación. Vuelve a iniciar sesión."
        404 -> "El registro ya no está disponible o aún no tiene inventario."
        409 -> "El backend rechazó el cambio. Revisa el stock o el estado actual."
        413 -> "La fotografía supera el tamaño permitido."
        415 -> "No se pudo validar la fotografía. Elige otra imagen."
        else -> porDefecto
    }
