package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.oylfrutossecos.ui.viewmodel.ProductoViewModel

@Composable
fun DashboardScreen(
    rol: String,
    modifier: Modifier = Modifier,
    productoViewModel: ProductoViewModel
) {

    // Productos observados desde Room mediante MVVM
    val productosRoom by productoViewModel.productos.collectAsState()

    // Adaptamos la entidad de Room al modelo que ya usan nuestras pantallas
    val productosCatalogo = productosRoom.map { producto ->
        ProductoCatalogo(
            id = producto.id,
            nombre = producto.nombre,
            categoria = producto.categoria,
            precio = producto.precio,
            stock = producto.stock,
            descripcion = producto.descripcion,
            presentacion = producto.presentacion
        )
    }

    var pantallaSeleccionada by rememberSaveable {
        mutableStateOf("Inicio")
    }

    var productoSeleccionado by remember {
        mutableStateOf<ProductoCatalogo?>(null)
    }

    val carrito = remember {
        mutableStateMapOf<Int, Int>()
    }

    val productosCarrito = remember {
        mutableStateMapOf<Int, ProductoCatalogo>()
    }

    var contadorPedidosDemo by rememberSaveable {
        mutableIntStateOf(0)
    }

    var numeroPedidoDemo by rememberSaveable {
        mutableStateOf("")
    }

    var totalPedidoDemo by rememberSaveable {
        mutableIntStateOf(0)
    }

    var modalidadPedidoDemo by rememberSaveable {
        mutableStateOf("")
    }

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

        // Contenido principal
        Box(
            modifier = Modifier.weight(1f)
        ) {

            when (pantallaSeleccionada) {

                "Inicio" -> {
                    InicioDashboard(
                        rol = rol,

                        onAbrirCatalogo = {
                            pantallaSeleccionada = "Catalogo"
                        },

                        onAbrirCarrito = {
                            pantallaSeleccionada = "Carrito"
                        },

                        onGestionProductos = {
                            pantallaSeleccionada = "GestionProductos"
                        }
                    )
                }

                "Mapa" -> {
                    MapaScreen(
                        modifier = Modifier.fillMaxSize()
                    )
                }

                "QR" -> {
                    QRScreen(
                        modifier = Modifier.fillMaxSize()
                    )
                }


                "GestionProductos" -> {
                    if (rol == "Administrador") {
                        GestionProductosScreen(
                            productos = productosCatalogo,

                            onVolver = {
                                pantallaSeleccionada = "Inicio"
                            },

                            onCrearProducto = { datos, onResultado ->
                                productoViewModel.crearProducto(
                                    datos,
                                    onResultado
                                )
                            },

                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                // NUEVA PANTALLA: CATALOGO

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


                "Carrito" -> {

                    CarritoScreen(
                        productos = productosCarrito.values.toList(),
                        cantidades = carrito,

                        onCambiarCantidad = { id, nuevaCantidad ->

                            val producto = productosCarrito[id]

                            if (nuevaCantidad <= 0) {

                                // Eliminar producto del carrito
                                carrito.remove(id)
                                productosCarrito.remove(id)

                            } else if (
                                producto != null &&
                                nuevaCantidad <= producto.stock
                            ) {

                                // Actualizar cantidad
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


                "Confirmacion" -> {

                    ConfirmacionPedidoScreen(
                        productos = productosCarrito.values.toList(),
                        cantidades = carrito,

                        onVolver = {
                            pantallaSeleccionada = "Carrito"
                        },

                        onConfirmar = { _, _, modalidad, _ ->

                            // Guardar el total antes de vaciar el carrito
                            totalPedidoDemo = productosCarrito.values.sumOf {
                                    producto ->
                                producto.precio * (carrito[producto.id] ?: 0)
                            }

                            // Crear número de solicitud ficticio
                            contadorPedidosDemo++

                            numeroPedidoDemo = "DEMO-" +
                                    contadorPedidosDemo.toString().padStart(4, '0')

                            modalidadPedidoDemo = modalidad

                            // Vaciar el carrito temporal
                            carrito.clear()
                            productosCarrito.clear()

                            // Mostrar la confirmación exitosa
                            pantallaSeleccionada = "PedidoExito"
                        },

                        modifier = Modifier.fillMaxSize()
                    )
                }

                "PedidoExito" -> {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),

                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "✓ Solicitud registrada",
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text("Número de pedido")

                        Text(
                            text = numeroPedidoDemo,
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Estado: Pendiente de Validación")

                        Text("Modalidad: $modalidadPedidoDemo")

                        Text("Total: $$totalPedidoDemo")

                        Spacer(modifier = Modifier.height(24.dp))

                        Text("Este pedido es una demostración académica.")

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                pantallaSeleccionada = "Inicio"
                            }
                        ) {
                            Text("Volver al inicio")
                        }
                    }
                }




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

        // Barra de navegación inferior
        NavigationBar {

            opciones.forEach { (nombre, icono) ->

                NavigationBarItem(
                    selected = pantallaSeleccionada == nombre ||
                            (nombre == "Inicio" &&
                                    pantallaSeleccionada == "Catalogo"),

                    onClick = {
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

@Composable
fun InicioDashboard(
    rol: String,
    onAbrirCatalogo: () -> Unit,
    onAbrirCarrito: () -> Unit,
    onGestionProductos: () -> Unit
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
                "Pedidos pendientes",
                "Revisa las solicitudes de compra."
            )

            TarjetaDashboard(
                "Stock bajo",
                "Productos que necesitan reposición."
            )

            TarjetaDashboard(
                "Gestionar productos",
                "Agregar y editar productos.",
                onClick = onGestionProductos

            )

            TarjetaDashboard(
                "Inventario",
                "Consultar y modificar existencias."
            )

        } else {

            // ESTA TARJETA AHORA ABRE EL CATALOGO
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
                .clickable { onClick() }
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
