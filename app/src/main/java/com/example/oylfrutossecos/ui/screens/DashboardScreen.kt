package com.example.oylfrutossecos.ui.screens

import android.widget.Toast

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oylfrutossecos.ui.viewmodel.CarritoViewModel
import com.example.oylfrutossecos.ui.viewmodel.PedidosViewModel
import com.example.oylfrutossecos.ui.components.ProductoImagen
import com.example.oylfrutossecos.ui.theme.VerdeOyl
import com.example.oylfrutossecos.ui.theme.VerdeOylOscuro
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.oylfrutossecos.ui.viewmodel.ClienteViewModel
import com.example.oylfrutossecos.ui.viewmodel.ProductoViewModel
import com.example.oylfrutossecos.data.local.PedidoLocalStore
import com.example.oylfrutossecos.data.local.PedidoLocal
import com.example.oylfrutossecos.data.local.DetallePedidoLocal

import com.example.oylfrutossecos.ui.validation.ValidadorCarrito
import com.example.oylfrutossecos.feature.navegacion.domain.ReglasNavegacionDashboard
import com.example.oylfrutossecos.ui.components.BarraNavegacionOYL
import com.example.oylfrutossecos.ui.components.AccesoRapidoCarritoOYL
import com.example.oylfrutossecos.feature.pedidos.domain.PreparadorDetallesPedido

@Composable
fun DashboardScreen(
    rol: String,
    modifier: Modifier = Modifier,
    productoViewModel: ProductoViewModel,
    clienteViewModel: ClienteViewModel,
    clienteId: Int?,
    onCerrarSesion: () -> Unit
) {


    // Datos del cliente autenticado
    LaunchedEffect(rol, clienteId) {
        if (rol == "Cliente" && clienteId != null) {
            clienteViewModel.cargarCliente(clienteId)
        }
    }
    val clienteActual by clienteViewModel.clienteActual.collectAsState()

    // Productos observados desde Room mediante MVVM
    val productosRoom by productoViewModel.productos.collectAsState()

    // Acceso al almacenamiento del dispositivo
    val context = LocalContext.current

    // ViewModel de pedidos. La pantalla solo observa los cambios.
    val pedidosViewModel: PedidosViewModel = viewModel()
    LaunchedEffect(context) {
        pedidosViewModel.cargar(context)
    }
    val pedidosGuardados = pedidosViewModel.pedidos

    // Adaptamos la entidad de Room al modelo de nuestras pantallas

    val productosCatalogo = productosRoom.map { producto ->
        ProductoCatalogo(
            id = producto.id,
            nombre = producto.nombre,
            categoria = producto.categoria,
            precio = producto.precio,
            stock = producto.stock,
            descripcion = producto.descripcion,
            presentacion = producto.presentacion,

            // Datos adicionales recuperados de Room
            imagenUri = producto.imagenUri,
            activo = producto.activo
        )
    }


    var pantallaSeleccionada by rememberSaveable {
        mutableStateOf("Inicio")
    }

    // Producto que llega desde el escáner QR
    var idProductoDesdeQR by rememberSaveable {
        mutableStateOf<Int?>(null)
    }

    var filtroInventarioInicial by rememberSaveable {
        mutableStateOf("Todos")
    }

    var filtroPedidosInicial by rememberSaveable {
        mutableStateOf("Todos")
    }

    var productoSeleccionado by remember {
        mutableStateOf<ProductoCatalogo?>(null)
    }

    // ViewModel separado para el carrito de cada cliente.
    // La clave evita compartir el mismo carrito entre diferentes clientes.
    val carritoViewModel: CarritoViewModel = viewModel(
        key = "carrito_${clienteId ?: "administrador"}"
    )
    val carrito = carritoViewModel.cantidades
    val productosCarrito = carritoViewModel.productos

    // Validar el carrito con los datos actuales de Room
    fun validarYActualizarCarrito(): String? {

        val error = ValidadorCarrito.validar(
            cantidades = carrito.toMap(),

            preciosCarrito = productosCarrito.mapValues { (_, producto) ->
                producto.precio
            },

            productosActuales = productosRoom
        )

        // Si algo cambió, actualizar la información visual
        // del carrito para que el Cliente pueda corregirlo.
        if (error != null) {

            val productosActuales = productosCatalogo.associateBy {
                it.id
            }

            carrito.keys.toList().forEach { id ->

                productosActuales[id]?.let { productoActual ->

                    productosCarrito[id] = productoActual
                }
            }
        }

        return error
    }

    // Información para la pantalla de pedido exitoso
    var numeroPedidoDemo by rememberSaveable {
        mutableStateOf("")
    }

    var totalPedidoDemo by rememberSaveable {
        mutableIntStateOf(0)
    }

    var modalidadPedidoDemo by rememberSaveable {
        mutableStateOf("")
    }


    // La cantidad se obtiene del mismo ViewModel que utiliza CarritoScreen.
    // Se actualiza automaticamente al agregar, modificar o quitar productos.
    val unidadesEnCarrito = carrito.values.sum()

    Column(
        modifier = modifier.fillMaxSize()
    ) {

        // ACCESO RAPIDO AL CARRITO - SOLO PARA CLIENTES.
        // Ocupa una barra propia para no tapar las pantallas ni el mapa.
        // No se muestra al confirmar un pedido ni dentro del carrito.

        // ACCESO RAPIDO AL CARRITO - SOLO CLIENTES.
        // La regla decide cuándo aparece.
        // El componente se encarga del diseño.
        if (
            ReglasNavegacionDashboard.mostrarAccesoCarrito(
                rol = rol,
                pantallaActual = pantallaSeleccionada
            )
        ) {

            AccesoRapidoCarritoOYL(
                unidadesEnCarrito = unidadesEnCarrito,

                onAbrirCarrito = {
                    pantallaSeleccionada = "Carrito"
                }
            )
        }



        Box(
            modifier = Modifier.weight(1f)
        ) {

            when (pantallaSeleccionada) {

                // INICIO
                "Inicio" -> {

                    InicioDashboardOYL(
                        rol = rol,
                        nombreCliente = clienteActual?.takeIf { it.id == clienteId }?.nombre.orEmpty(),
                        productosDestacados = productosCatalogo,
                        onAbrirMapa = { pantallaSeleccionada = "Mapa" },
                        onAbrirQr = { pantallaSeleccionada = "QR" },

                        totalAvisos = pedidosGuardados.count {
                            it.estado == "Pendiente"
                        } + productosRoom.count {
                            it.activo && it.stock in 0..10
                        },

                        onAbrirNotificaciones = {
                            pantallaSeleccionada = "Notificaciones"
                        },


                        pedidosPendientes = pedidosGuardados.count {
                            it.estado == "Pendiente"
                        },

                        onVerPedidosPendientes = {
                            filtroPedidosInicial = "Pendiente"
                            pantallaSeleccionada = "Pedidos"
                        },


                        productosStockBajo = productosRoom.count {
                            it.activo && it.stock in 1..10
                        },

                        productosAgotados = productosRoom.count {
                            it.activo && it.stock == 0
                        },

                        onVerAlertasStock = {
                            idProductoDesdeQR = null
                            filtroInventarioInicial = "Por reponer"
                            pantallaSeleccionada = "GestionInventario"
                        },

                        onAbrirCatalogo = {
                            pantallaSeleccionada = "Catalogo"
                        },

                        onAbrirCarrito = {
                            pantallaSeleccionada = "Carrito"
                        },

                        onVerMisPedidos = {
                            pantallaSeleccionada = "Pedidos"
                        },

                        onGestionProductos = {
                            pantallaSeleccionada = "GestionProductos"
                        },

                        onGestionInventario = {
                            idProductoDesdeQR = null
                            filtroInventarioInicial = "Todos"
                            pantallaSeleccionada = "GestionInventario"
                        }
                    )
                }

                // MAPA
                "Mapa" -> {

                    MapaScreen(
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // ESCÁNER QR
                "QR" -> {

                    val estadosProductos = productosRoom.associate {
                        it.id to it.activo
                    }

                    QRScreen(
                        productos = productosCatalogo,

                        estadosProductos = estadosProductos,

                        rol = rol,

                        onVerProducto = { producto ->
                            productoSeleccionado = producto
                            pantallaSeleccionada = "Detalle"
                        },

                        onAbrirInventario = { idProducto ->
                            idProductoDesdeQR = idProducto
                            filtroInventarioInicial = "Todos"
                            pantallaSeleccionada = "GestionInventario"
                        },

                        modifier = Modifier.fillMaxSize()
                    )
                }

                // GESTIÓN DE PRODUCTOS
                "GestionProductos" -> {

                    if (rol == "Administrador") {

                        val estadosProductos = productosRoom.associate {
                            it.id to it.activo
                        }

                        val imagenesProductos = productosRoom.associate {
                            it.id to it.imagenUri
                        }

                        GestionProductosScreen(
                            productos = productosCatalogo,

                            estadosProductos = estadosProductos,

                            imagenesProductos = imagenesProductos,

                            onVolver = {
                                pantallaSeleccionada = "Inicio"
                            },

                            onCrearProducto = { datos, onResultado ->
                                productoViewModel.crearProducto(
                                    datos,
                                    onResultado
                                )
                            },

                            onEditarProducto = { id, datos, onResultado ->
                                productoViewModel.editarProducto(
                                    id,
                                    datos,
                                    onResultado
                                )
                            },

                            onCambiarEstado = { id, activo, onResultado ->
                                productoViewModel.cambiarEstadoProducto(
                                    id,
                                    activo,
                                    onResultado
                                )
                            },

                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // GESTIÓN DE INVENTARIO
                "GestionInventario" -> {

                    if (rol == "Administrador") {

                        val estadosProductos = productosRoom.associate {
                            it.id to it.activo
                        }

                        GestionInventarioScreen(
                            productos = productosCatalogo,

                            estadosProductos = estadosProductos,

                            productoInicialId = idProductoDesdeQR,

                            filtroInicial = filtroInventarioInicial,

                            onVolver = {
                                pantallaSeleccionada = "Inicio"
                            },

                            onAjustarStock = {
                                    id,
                                    cantidadTexto,
                                    esEntrada,
                                    onResultado ->

                                productoViewModel.ajustarStock(
                                    id = id,
                                    cantidadTexto = cantidadTexto,
                                    esEntrada = esEntrada,
                                    onResultado = onResultado
                                )
                            },

                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

// PANTALLA 15: PERFIL
                "Perfil" -> {

                    if (rol == "Administrador") {

                        PerfilAdministradorScreen(

                            productosActivos = productosRoom.count {
                                it.activo
                            },

                            alertasStock = productosRoom.count {
                                it.activo && it.stock in 0..10
                            },

                            pedidosPendientes = pedidosGuardados.count {
                                it.estado == "Pendiente"
                            },

                            pedidosRegistrados = pedidosGuardados.size,

                            onVolver = {
                                pantallaSeleccionada = "Inicio"
                            },

                            onGestionProductos = {
                                pantallaSeleccionada = "GestionProductos"
                            },

                            onGestionInventario = {
                                idProductoDesdeQR = null
                                filtroInventarioInicial = "Todos"
                                pantallaSeleccionada = "GestionInventario"
                            },

                            onGestionPedidos = {
                                filtroPedidosInicial = "Todos"
                                pantallaSeleccionada = "Pedidos"
                            },

                            onNotificaciones = {
                                pantallaSeleccionada = "Notificaciones"
                            },

                            onCerrarSesion = {
                                carritoViewModel.vaciar()
                                clienteViewModel.limpiarClienteActual()
                                onCerrarSesion()
                            },

                            modifier = Modifier.fillMaxSize()
                        )

                    } else {

                        PerfilScreen(
                            cliente = clienteActual?.takeIf { it.id == clienteId },
                            clienteViewModel = clienteViewModel,
                            modifier = Modifier.fillMaxSize(),
                            onCerrarSesion = {
                                carritoViewModel.vaciar()
                                clienteViewModel.limpiarClienteActual()
                                onCerrarSesion()
                            }
                        )

                    }
                }


                // PANTALLA 14: NOTIFICACIONES
                "Notificaciones" -> {

                    if (rol == "Administrador") {

                        val estadosProductos = productosRoom.associate {
                            it.id to it.activo
                        }

                        NotificacionesScreen(
                            productos = productosCatalogo,
                            estadosProductos = estadosProductos,
                            pedidos = pedidosGuardados,

                            onVolver = {
                                pantallaSeleccionada = "Inicio"
                            },

                            onAbrirInventario = { filtro ->

                                idProductoDesdeQR = null
                                filtroInventarioInicial = filtro

                                pantallaSeleccionada = "GestionInventario"
                            },

                            onAbrirPedidos = {

                                filtroPedidosInicial = "Pendiente"
                                pantallaSeleccionada = "Pedidos"
                            },

                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }


                // GESTIÓN DE PEDIDOS
                "Pedidos" -> {

                    if (rol == "Administrador") {

                        GestionPedidosScreen(
                            pedidos = pedidosGuardados,

                            filtroInicial = filtroPedidosInicial,

                            onVolver = {
                                pantallaSeleccionada = "Inicio"
                            },

                            onCambiarEstado = {
                                    numero,
                                    nuevoEstado,
                                    onResultado ->

                                try {

                                    pedidosViewModel.actualizarEstado(
                                        context = context,
                                        numero = numero,
                                        nuevoEstado = nuevoEstado
                                    )

                                    onResultado(null)

                                } catch (error: Exception) {

                                    onResultado(
                                        "No se pudo actualizar el estado del pedido."
                                    )
                                }
                            },

                            modifier = Modifier.fillMaxSize()
                        )

                    } else {
                        // Historial individual del Cliente autenticado
                        MisPedidosScreen(
                            pedidos = pedidosGuardados,
                            clienteId = clienteId,
                            onVolver = {
                                pantallaSeleccionada = "Inicio"
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // CATÁLOGO
                "Catalogo" -> {

                    CatalogoScreen(
                        productos = productosCatalogo,

                        modifier = Modifier.fillMaxSize(),

                        onVerDetalle = { producto ->
                            productoSeleccionado = producto
                            pantallaSeleccionada = "Detalle"
                        }
                    )
                }

                // DETALLE DE PRODUCTO
                "Detalle" -> {

                    val producto = productoSeleccionado

                    if (producto != null) {

                        DetalleProductoScreen(
                            producto = producto,

                            rol = rol,

                            cantidadYaAgregada =
                                carrito[producto.id] ?: 0,

                            onVolver = {
                                pantallaSeleccionada = "Catalogo"
                            },

                            onAgregar = { cantidad ->
                                carritoViewModel.agregar(producto, cantidad)
                            },

                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // CARRITO
                "Carrito" -> {

                    CarritoScreen(
                        productos = productosCarrito.values.toList(),

                        productosActuales = productosCatalogo,

                        cantidades = carrito,


                        onCambiarCantidad = { id, nuevaCantidad ->
                            val productoActual = productosCatalogo.firstOrNull { it.id == id }
                            carritoViewModel.cambiarCantidad(productoActual, id, nuevaCantidad)
                        },


                        onVolverCatalogo = {
                            pantallaSeleccionada = "Catalogo"
                        },


                        onConfirmarPedido = {

                            val error = validarYActualizarCarrito()

                            if (error == null) {

                                // Todos los productos siguen disponibles
                                pantallaSeleccionada = "Confirmacion"

                            } else {

                                // Mostrar el problema sin perder el carrito
                                Toast.makeText(
                                    context,
                                    error,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },


                        modifier = Modifier.fillMaxSize()
                    )
                }

                // CONFIRMACIÓN DE PEDIDO
                "Confirmacion" -> {

                    ConfirmacionPedidoScreen(
                        productos = productosCarrito.values.toList(),

                        cantidades = carrito,

                        onVolver = {
                            pantallaSeleccionada = "Carrito"
                        },


                        onConfirmar = {
                                nombre,
                                telefono,
                                modalidad,
                                direccion ->

                            // Verificar nuevamente el carrito
                            // justo antes de registrar la solicitud
                            val errorValidacion = validarYActualizarCarrito()

                            if (errorValidacion != null) {

                                // Avisar al Cliente qué cambió
                                Toast.makeText(
                                    context,
                                    errorValidacion,
                                    Toast.LENGTH_LONG
                                ).show()

                                // Regresar al carrito para corregirlo
                                // sin eliminar sus productos
                                pantallaSeleccionada = "Carrito"

                            } else {

                                // Solo registrar si la validación fue exitosa
                                try {

// Preparar los detalles desde el módulo de pedidos
                                    val detallesPedido = PreparadorDetallesPedido.desdeCarrito(
                                        productos = productosCarrito.values.toList(),
                                        cantidades = carrito.toMap()
                                    )

                                    // Guardar el pedido localmente
                                    val pedido = pedidosViewModel.registrar(
                                        context = context,
                                        clienteId = clienteId,
                                        nombreCliente = nombre,
                                        telefono = telefono,
                                        modalidad = modalidad,
                                        direccion = direccion,
                                        productos = detallesPedido
                                    )

                                    // Preparar la pantalla de éxito
                                    numeroPedidoDemo = pedido.numero
                                    totalPedidoDemo = pedido.total
                                    modalidadPedidoDemo = pedido.modalidad

                                    // Vaciar solo despues de confirmar el guardado.
                                    carritoViewModel.vaciar()

                                    // Mostrar confirmación animada
                                    pantallaSeleccionada = "PedidoExito"

                                } catch (error: Exception) {

                                    // Si falla el guardado, conservar el carrito
                                    Toast.makeText(
                                        context,
                                        "No se pudo guardar el pedido. " +
                                                "El carrito se conserva.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        },


                        modifier = Modifier.fillMaxSize()
                    )
                }


                // PEDIDO REGISTRADO EXITOSAMENTE
                "PedidoExito" -> {

                    PedidoExitoScreen(
                        numeroPedido = numeroPedidoDemo,

                        modalidad = modalidadPedidoDemo,

                        total = totalPedidoDemo,

                        onVolverInicio = {
                            pantallaSeleccionada = "Inicio"
                        },

                        modifier = Modifier.fillMaxSize()
                    )
                }



                // OTRAS PANTALLAS PENDIENTES
                else -> {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "Pantalla: $pantallaSeleccionada",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            }
        }


// Barra inferior modularizada
        BarraNavegacionOYL(
            pantallaActual = pantallaSeleccionada,

            onSeleccionar = { destino ->

                // Al entrar a pedidos desde la barra,
                // volver a mostrar todos los pedidos.
                if (destino == "Pedidos") {
                    filtroPedidosInicial = "Todos"
                }

                pantallaSeleccionada = destino
            }
        )

    }
}
