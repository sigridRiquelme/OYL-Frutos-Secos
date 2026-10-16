
package com.example.oylfrutossecos.ui.screens

import android.widget.Toast

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import com.example.oylfrutossecos.ui.viewmodel.ProductoViewModel
import com.example.oylfrutossecos.data.local.PedidoLocalStore
import com.example.oylfrutossecos.data.local.PedidoLocal
import com.example.oylfrutossecos.data.local.DetallePedidoLocal

@Composable
fun DashboardScreen(
    rol: String,
    modifier: Modifier = Modifier,
    productoViewModel: ProductoViewModel,
    onCerrarSesion: () -> Unit
) {


    // Productos observados desde Room mediante MVVM
    val productosRoom by productoViewModel.productos.collectAsState()

    // Acceso al almacenamiento del dispositivo
    val context = LocalContext.current

    // Pedidos registrados localmente
    var pedidosGuardados by remember(context) {
        mutableStateOf<List<PedidoLocal>>(
            PedidoLocalStore.obtenerPedidos(context)
        )
    }

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

    // Carrito del cliente
    val carrito = remember {
        mutableStateMapOf<Int, Int>()
    }

    val productosCarrito = remember {
        mutableStateMapOf<Int, ProductoCatalogo>()
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

    // Navegación inferior
    val opciones = listOf(
        "Inicio" to "🏠",
        "Mapa" to "📍",
        "QR" to "▣",
        "Pedidos" to "📦",
        "Perfil" to "👤"
    )

    Column(
        modifier = modifier.fillMaxSize()
    ) {

        Box(
            modifier = Modifier.weight(1f)
        ) {

            when (pantallaSeleccionada) {

                // INICIO
                "Inicio" -> {

                    InicioDashboard(
                        rol = rol,

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

                            onCerrarSesion = onCerrarSesion,

                            modifier = Modifier.fillMaxSize()
                        )

                    } else {

                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Perfil del Cliente pendiente de integración.")
                        }
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

                                    pedidosGuardados =
                                        PedidoLocalStore.actualizarEstado(
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

                        // La pantalla de pedidos específica del Cliente
                        // se implementará en una etapa posterior
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Pantalla: Pedidos")
                        }
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

                                val actual = carrito[producto.id] ?: 0

                                val maximo = (
                                        producto.stock - actual
                                        ).coerceAtLeast(0)

                                if (cantidad in 1..maximo) {

                                    carrito[producto.id] = actual + cantidad

                                    productosCarrito[producto.id] = producto
                                }
                            },

                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // CARRITO
                "Carrito" -> {

                    CarritoScreen(
                        productos = productosCarrito.values.toList(),

                        cantidades = carrito,

                        onCambiarCantidad = { id, nuevaCantidad ->

                            val producto = productosCarrito[id]

                            if (nuevaCantidad <= 0) {

                                carrito.remove(id)
                                productosCarrito.remove(id)

                            } else if (
                                producto != null &&
                                nuevaCantidad <= producto.stock
                            ) {

                                carrito[id] = nuevaCantidad
                            }
                        },

                        onVolverCatalogo = {
                            pantallaSeleccionada = "Catalogo"
                        },

                        onConfirmarPedido = {
                            pantallaSeleccionada = "Confirmacion"
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

                            try {

                                // Preparar los productos de la compra
                                val detallesPedido =
                                    productosCarrito.values.mapNotNull { producto ->

                                        val cantidad =
                                            carrito[producto.id] ?: 0

                                        if (cantidad > 0) {

                                            DetallePedidoLocal(
                                                idProducto = producto.id,
                                                nombre = producto.nombre,
                                                cantidad = cantidad,
                                                precioUnitario = producto.precio
                                            )

                                        } else {

                                            null
                                        }
                                    }

                                // Guardar realmente el pedido
                                val pedido = PedidoLocalStore.registrarPedido(
                                    context = context,
                                    nombreCliente = nombre,
                                    telefono = telefono,
                                    modalidad = modalidad,
                                    direccion = direccion,
                                    productos = detallesPedido
                                )

                                // Actualizar historial de pedidos
                                pedidosGuardados = pedidosGuardados + pedido

                                // Guardar datos para pantalla de éxito
                                numeroPedidoDemo = pedido.numero
                                totalPedidoDemo = pedido.total
                                modalidadPedidoDemo = pedido.modalidad

                                // Vaciar carrito SOLO después de guardar
                                carrito.clear()
                                productosCarrito.clear()

                                // Mostrar confirmación exitosa
                                pantallaSeleccionada = "PedidoExito"

                            } catch (error: Exception) {

                                // Si falla, conservar el carrito
                                Toast.makeText(
                                    context,
                                    "No se pudo guardar el pedido. " +
                                            "El carrito se conserva.",
                                    Toast.LENGTH_LONG
                                ).show()
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

        // BARRA DE NAVEGACIÓN INFERIOR
        NavigationBar {

            opciones.forEach { (nombre, icono) ->

                NavigationBarItem(
                    selected =
                        pantallaSeleccionada == nombre ||
                                (
                                        nombre == "Inicio" &&
                                                pantallaSeleccionada == "Catalogo"
                                        ),


                    onClick = {

                        if (nombre == "Pedidos") {
                            filtroPedidosInicial = "Todos"
                        }

                        pantallaSeleccionada = nombre
                    },


                    icon = {
                        Text(icono)
                    },

                    label = {
                        Text(nombre)
                    }
                )
            }
        }
    }
}


// DASHBOARD PRINCIPAL

@Composable
fun InicioDashboard(
    rol: String,
    onAbrirCatalogo: () -> Unit,
    onAbrirCarrito: () -> Unit,
    onGestionProductos: () -> Unit,
    onGestionInventario: () -> Unit,
    productosStockBajo: Int,
    productosAgotados: Int,
    onVerAlertasStock: () -> Unit,
    pedidosPendientes: Int,
    onVerPedidosPendientes: () -> Unit,
    totalAvisos: Int,
    onAbrirNotificaciones: () -> Unit
) {



    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),

        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "OYL Frutos Secos",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Bienvenido, $rol"
        )

        if (rol == "Administrador") {

            TarjetaDashboard(
                titulo = "Notificaciones ($totalAvisos)",

                descripcion = if (totalAvisos == 0) {
                    "No tienes notificaciones activas."
                } else {
                    "Tienes $totalAvisos avisos de pedidos " +
                            "e inventario. Pulsa para revisarlos."
                },

                onClick = onAbrirNotificaciones
            )

            TarjetaDashboard(
                titulo = "Pedidos pendientes ($pedidosPendientes)",

                descripcion = if (pedidosPendientes == 0) {
                    "No tienes pedidos pendientes."
                } else {
                    "Tienes $pedidosPendientes pedidos esperando atención. " +
                            "Pulsa para revisarlos."
                },

                onClick = onVerPedidosPendientes
            )


            val totalAlertas =
                productosStockBajo + productosAgotados




            TarjetaDashboard(
                titulo = "Productos por reponer ($totalAlertas)",

                descripcion = if (totalAlertas == 0) {

                    "Sin alertas de stock."

                } else {

                    "$productosStockBajo con stock bajo " +
                            "y $productosAgotados agotados. " +
                            "Pulsa para consultar."
                },

                onClick = onVerAlertasStock
            )

            TarjetaDashboard(
                titulo = "Gestionar productos",
                descripcion = "Agregar y editar productos.",
                onClick = onGestionProductos
            )

            TarjetaDashboard(
                titulo = "Inventario",
                descripcion = "Consultar existencias y alertas de stock.",
                onClick = onGestionInventario
            )

        } else {

            TarjetaDashboard(
                titulo = "Productos destacados",
                descripcion = "Descubre nuestros productos naturales.",
                onClick = onAbrirCatalogo
            )

            TarjetaDashboard(
                "Promociones",
                "Revisa nuestras ofertas disponibles."
            )

            TarjetaDashboard(
                titulo = "Mi carrito",
                descripcion = "Consulta tus productos seleccionados.",
                onClick = onAbrirCarrito
            )

            TarjetaDashboard(
                "Mis pedidos",
                "Revisa tus compras."
            )
        }
    }
}


// COMPONENTE REUTILIZABLE PARA TARJETAS
@Composable
fun TarjetaDashboard(
    titulo: String,
    descripcion: String,
    onClick: (() -> Unit)? = null
) {

    ElevatedCard(
        modifier = if (onClick != null) {

            Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                }

        } else {

            Modifier.fillMaxWidth()
        }
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = descripcion
            )
        }
    }
}
