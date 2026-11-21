
package com.example.oylfrutossecos.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.oylfrutossecos.ui.viewmodel.CarritoViewModel
import com.example.oylfrutossecos.ui.viewmodel.PedidosViewModel
import com.example.oylfrutossecos.ui.viewmodel.ClienteViewModel
import com.example.oylfrutossecos.ui.viewmodel.ProductoViewModel

import com.example.oylfrutossecos.ui.validation.ValidadorCarrito

import com.example.oylfrutossecos.feature.carrito.domain.CoordinadorRevisionCarrito

import com.example.oylfrutossecos.feature.pedidos.domain.CoordinadorOperacionesPedido
import com.example.oylfrutossecos.feature.pedidos.domain.ResultadoConfirmacionPedido

import com.example.oylfrutossecos.feature.administracion.domain.CalculadoraResumenAdministrativo

import com.example.oylfrutossecos.feature.navegacion.domain.ReglasNavegacionDashboard
import com.example.oylfrutossecos.feature.navegacion.domain.AccionCliente
import com.example.oylfrutossecos.feature.navegacion.domain.NavegacionCliente

import com.example.oylfrutossecos.ui.components.BarraNavegacionOYL
import com.example.oylfrutossecos.ui.components.AccesoRapidoCarritoOYL
import com.example.oylfrutossecos.ui.components.PantallasExploracionCliente

@Composable
fun DashboardScreen(
    rol: String,
    modifier: Modifier = Modifier,
    productoViewModel: ProductoViewModel,
    clienteViewModel: ClienteViewModel,
    clienteId: Int?,
    onCerrarSesion: () -> Unit
) {

    // DATOS DEL CLIENTE AUTENTICADO
    LaunchedEffect(rol, clienteId) {
        if (rol == "Cliente" && clienteId != null) {
            clienteViewModel.cargarCliente(clienteId)
        }
    }

    val clienteActual by clienteViewModel.clienteActual.collectAsState()

    // PRODUCTOS OBTENIDOS DESDE ROOM
    val productosRoom by productoViewModel.productos.collectAsState()

    val context = LocalContext.current

    // PEDIDOS
    val pedidosViewModel: PedidosViewModel = viewModel()

    LaunchedEffect(context) {
        pedidosViewModel.cargar(context)
    }

    val pedidosGuardados = pedidosViewModel.pedidos

    // ADAPTAR PRODUCTOS DE ROOM AL CATALOGO
    val productosCatalogo = productosRoom.map { producto ->

        ProductoCatalogo(
            id = producto.id,
            nombre = producto.nombre,
            categoria = producto.categoria,
            precio = producto.precio,
            stock = producto.stock,
            descripcion = producto.descripcion,
            presentacion = producto.presentacion,
            imagenUri = producto.imagenUri,
            activo = producto.activo
        )
    }

    // ESTADOS DE NAVEGACION
    var pantallaSeleccionada by rememberSaveable {
        mutableStateOf("Inicio")
    }

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

    // CARRITO ASOCIADO AL CLIENTE
    val carritoViewModel: CarritoViewModel = viewModel(
        key = "carrito_${clienteId ?: "administrador"}"
    )

    val carrito = carritoViewModel.cantidades
    val productosCarrito = carritoViewModel.productos

    // VALIDAR Y ACTUALIZAR EL CARRITO
    fun validarYActualizarCarrito(): String? {

        val resultado = CoordinadorRevisionCarrito.revisar(
            cantidades = carrito.toMap(),
            productosGuardados = productosCarrito.toMap(),
            productosActuales = productosCatalogo,

            validar = { cantidades, preciosOriginales ->

                ValidadorCarrito.validar(
                    cantidades = cantidades,
                    preciosCarrito = preciosOriginales,
                    productosActuales = productosRoom
                )
            }
        )

        if (resultado.error != null) {
            productosCarrito.putAll(
                resultado.productosActualizados
            )
        }

        return resultado.error
    }

    // INFORMACION DEL PEDIDO EXITOSO
    var numeroPedidoDemo by rememberSaveable {
        mutableStateOf("")
    }

    var totalPedidoDemo by rememberSaveable {
        mutableIntStateOf(0)
    }

    var modalidadPedidoDemo by rememberSaveable {
        mutableStateOf("")
    }

    // TOTAL DE UNIDADES DEL CARRITO
    val unidadesEnCarrito = carrito.values.sum()

    // RESUMEN ADMINISTRATIVO MODULARIZADO
    val resumenAdministrativo =
        CalculadoraResumenAdministrativo.calcular(
            productos = productosCatalogo,
            pedidos = pedidosGuardados
        )

    // CONTENEDOR PRINCIPAL
    Column(
        modifier = modifier.fillMaxSize()
    ) {

        // ACCESO RAPIDO AL CARRITO
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

        // CONTENIDO DE LAS PANTALLAS
        Box(
            modifier = Modifier.weight(1f)
        ) {

            when (pantallaSeleccionada) {

                // INICIO
                "Inicio" -> {

                    InicioDashboardOYL(
                        rol = rol,

                        nombreCliente = clienteActual
                            ?.takeIf { it.id == clienteId }
                            ?.nombre
                            .orEmpty(),

                        productosDestacados = productosCatalogo,

                        onAbrirMapa = {
                            pantallaSeleccionada = "Mapa"
                        },

                        onAbrirQr = {
                            pantallaSeleccionada = "QR"
                        },

                        totalAvisos = resumenAdministrativo.totalAvisos,

                        onAbrirNotificaciones = {
                            pantallaSeleccionada = "Notificaciones"
                        },

                        pedidosPendientes =
                            resumenAdministrativo.pedidosPendientes,

                        onVerPedidosPendientes = {
                            filtroPedidosInicial = "Pendiente"
                            pantallaSeleccionada = "Pedidos"
                        },

                        productosStockBajo =
                            resumenAdministrativo.productosStockBajo,

                        productosAgotados =
                            resumenAdministrativo.productosAgotados,

                        onVerAlertasStock = {
                            idProductoDesdeQR = null
                            filtroInventarioInicial = "Por reponer"
                            pantallaSeleccionada = "GestionInventario"
                        },

                        // NAVEGACION MODULARIZADA
                        onAbrirCatalogo = {

                            pantallaSeleccionada =
                                NavegacionCliente.siguientePantalla(
                                    pantallaSeleccionada,
                                    AccionCliente.ABRIR_CATALOGO
                                )
                        },

                        onAbrirCarrito = {

                            pantallaSeleccionada =
                                NavegacionCliente.siguientePantalla(
                                    pantallaSeleccionada,
                                    AccionCliente.ABRIR_CARRITO
                                )
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

                // ESCANER QR
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

                            pantallaSeleccionada =
                                NavegacionCliente.siguientePantalla(
                                    pantallaSeleccionada,
                                    AccionCliente.VER_DETALLE
                                )
                        },

                        onAbrirInventario = { idProducto ->
                            idProductoDesdeQR = idProducto
                            filtroInventarioInicial = "Todos"
                            pantallaSeleccionada = "GestionInventario"
                        },

                        modifier = Modifier.fillMaxSize()
                    )
                }

                // GESTION DE PRODUCTOS
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

                // GESTION DE INVENTARIO
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

                // PERFIL
                "Perfil" -> {

                    if (rol == "Administrador") {

                        PerfilAdministradorScreen(
                            productosActivos =
                                resumenAdministrativo.productosActivos,

                            alertasStock =
                                resumenAdministrativo.alertasStock,

                            pedidosPendientes =
                                resumenAdministrativo.pedidosPendientes,

                            pedidosRegistrados =
                                resumenAdministrativo.pedidosRegistrados,

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
                            cliente = clienteActual
                                ?.takeIf { it.id == clienteId },

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

                // NOTIFICACIONES ADMINISTRATIVAS
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

                // PEDIDOS
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

                                val mensajeError =
                                    CoordinadorOperacionesPedido
                                        .cambiarEstado {

                                            pedidosViewModel.actualizarEstado(
                                                context = context,
                                                numero = numero,
                                                nuevoEstado = nuevoEstado
                                            )
                                        }

                                onResultado(mensajeError)
                            },

                            modifier = Modifier.fillMaxSize()
                        )

                    } else {

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

                // CATALOGO Y DETALLE MODULARIZADOS
                "Catalogo", "Detalle" -> {

                    PantallasExploracionCliente(
                        pantalla = pantallaSeleccionada,
                        productos = productosCatalogo,
                        productoSeleccionado = productoSeleccionado,
                        rol = rol,
                        cantidadesCarrito = carrito,

                        onVerDetalle = { producto ->

                            productoSeleccionado = producto

                            pantallaSeleccionada =
                                NavegacionCliente.siguientePantalla(
                                    pantallaSeleccionada,
                                    AccionCliente.VER_DETALLE
                                )
                        },

                        onVolverCatalogo = {

                            pantallaSeleccionada =
                                NavegacionCliente.siguientePantalla(
                                    pantallaSeleccionada,
                                    AccionCliente.VOLVER_A_CATALOGO
                                )
                        },

                        onAgregar = { producto, cantidad ->

                            carritoViewModel.agregar(
                                producto,
                                cantidad
                            )
                        },

                        modifier = Modifier.fillMaxSize()
                    )
                }

                // CARRITO
                "Carrito" -> {

                    CarritoScreen(
                        productos = productosCarrito.values.toList(),
                        productosActuales = productosCatalogo,
                        cantidades = carrito,

                        onCambiarCantidad = { id, nuevaCantidad ->

                            val productoActual = productosCatalogo
                                .firstOrNull { it.id == id }

                            carritoViewModel.cambiarCantidad(
                                productoActual,
                                id,
                                nuevaCantidad
                            )
                        },

                        onVolverCatalogo = {

                            pantallaSeleccionada =
                                NavegacionCliente.siguientePantalla(
                                    pantallaSeleccionada,
                                    AccionCliente.VOLVER_A_CATALOGO
                                )
                        },

                        onConfirmarPedido = {

                            val error = validarYActualizarCarrito()

                            if (error == null) {

                                pantallaSeleccionada =
                                    NavegacionCliente.siguientePantalla(
                                        pantallaSeleccionada,
                                        AccionCliente.CONTINUAR_A_CONFIRMACION
                                    )

                            } else {

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

                // CONFIRMACION DE PEDIDO
                "Confirmacion" -> {

                    ConfirmacionPedidoScreen(
                        productos = productosCarrito.values.toList(),
                        cantidades = carrito,

                        onVolver = {

                            pantallaSeleccionada =
                                NavegacionCliente.siguientePantalla(
                                    pantallaSeleccionada,
                                    AccionCliente.VOLVER_A_CARRITO
                                )
                        },

                        onConfirmar = {
                                nombre,
                                telefono,
                                modalidad,
                                direccion ->

                            val resultado =
                                CoordinadorOperacionesPedido.confirmar(

                                    validarCarrito = {
                                        validarYActualizarCarrito()
                                    },

                                    productos =
                                        productosCarrito.values.toList(),

                                    cantidades = carrito.toMap(),

                                    registrar = { detalles ->

                                        pedidosViewModel.registrar(
                                            context = context,
                                            clienteId = clienteId,
                                            nombreCliente = nombre,
                                            telefono = telefono,
                                            modalidad = modalidad,
                                            direccion = direccion,
                                            productos = detalles
                                        )
                                    }
                                )

                            when (resultado) {

                                is ResultadoConfirmacionPedido.CarritoInvalido -> {

                                    // Conservar carrito y mostrar problema.
                                    Toast.makeText(
                                        context,
                                        resultado.mensaje,
                                        Toast.LENGTH_LONG
                                    ).show()

                                    pantallaSeleccionada = "Carrito"
                                }

                                is ResultadoConfirmacionPedido.Registrado -> {

                                    val pedido = resultado.pedido

                                    numeroPedidoDemo = pedido.numero
                                    totalPedidoDemo = pedido.total
                                    modalidadPedidoDemo = pedido.modalidad

                                    // Vaciar solamente después de guardar.
                                    carritoViewModel.vaciar()

                                    pantallaSeleccionada =
                                        NavegacionCliente.siguientePantalla(
                                            pantallaSeleccionada,
                                            AccionCliente.PEDIDO_REGISTRADO
                                        )
                                }

                                ResultadoConfirmacionPedido.ErrorGuardado -> {

                                    // Si ocurre un error, conservar carrito.
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

                            pantallaSeleccionada =
                                NavegacionCliente.siguientePantalla(
                                    pantallaSeleccionada,
                                    AccionCliente.VOLVER_A_INICIO
                                )
                        },

                        modifier = Modifier.fillMaxSize()
                    )
                }

                // OTRAS PANTALLAS
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

        // BARRA DE NAVEGACION INFERIOR
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
