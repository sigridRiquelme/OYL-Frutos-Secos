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
import com.example.oylfrutossecos.data.repository.EstadoPedidoRemoto
import com.example.oylfrutossecos.data.repository.PedidosRemotos
import com.example.oylfrutossecos.data.repository.SesionRemota
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import retrofit2.HttpException

private enum class AreaAdministrativa(val etiqueta: String) {
    PRODUCTOS("Productos"), INVENTARIO("Inventario"), PEDIDOS("Pedidos")
}

@Composable
internal fun AdministracionConBackend(
    host: String,
    sesion: SesionRemota,
    modifier: Modifier = Modifier
) {
    val catalogo = remember(host) { CatalogoRemoto(host) }
    val pedidosRemotos = remember(host) { PedidosRemotos(host) }
    val alcance = rememberCoroutineScope()
    var area by rememberSaveable { mutableStateOf<AreaAdministrativa?>(null) }
    var productos by remember { mutableStateOf<List<ProductoCatalogo>?>(null) }
    var pedidos by remember { mutableStateOf<List<Pedido>?>(null) }
    var errorCarga by remember { mutableStateOf<String?>(null) }
    var aviso by remember { mutableStateOf<String?>(null) }
    var recarga by remember { mutableIntStateOf(0) }
    var guardando by remember { mutableStateOf(false) }

    fun abrir(destino: AreaAdministrativa?) {
        if (guardando) return
        productos = null
        pedidos = null
        errorCarga = null
        aviso = null
        if (area == destino) recarga++
        area = destino
    }

    fun actualizarProducto(producto: ProductoCatalogo) {
        productos = productos?.map { if (it.id == producto.id) producto else it }
    }

    fun guardar(resultado: (String?) -> Unit, operacion: suspend () -> Unit) {
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
                val mensaje = mensajeAdministracion(fallo, "No pudimos confirmar el cambio. Recarga antes de repetir la operación.")
                aviso = mensaje
                resultado(mensaje)
                val codigo = (fallo as? HttpException)?.code()
                if (codigo == null || codigo >= 500) {
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
                AreaAdministrativa.PRODUCTOS, AreaAdministrativa.INVENTARIO -> productos = catalogo.cargar()
                AreaAdministrativa.PEDIDOS -> pedidos = pedidosRemotos.listarParaAdministracion(sesion)
                null -> Unit
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
    val fallo = errorCarga
    BackHandler(enabled = destino != null) { abrir(null) }
    Column(modifier.fillMaxSize()) {
        if (guardando) LinearProgressIndicator(Modifier.fillMaxWidth())
        aviso?.let { Text(it, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.error) }
        when {
            destino == null -> Column(
                Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Administración", style = MaterialTheme.typography.headlineMedium)
                AreaAdministrativa.entries.forEach { opcion ->
                    Button(modifier = Modifier.fillMaxWidth(), onClick = { abrir(opcion) }) {
                        Text(opcion.etiqueta)
                    }
                }
            }
            fallo != null -> Column(Modifier.padding(20.dp)) {
                Text(fallo, color = MaterialTheme.colorScheme.error)
                Button(onClick = { abrir(destino) }) { Text("Recargar") }
                TextButton(onClick = { abrir(null) }) { Text("Volver") }
            }
            destino == AreaAdministrativa.PRODUCTOS && listaProductos != null -> GestionProductosScreen(
                productos = listaProductos,
                estadosProductos = listaProductos.associate { it.id to it.activo },
                imagenesProductos = listaProductos.associate { it.id to it.imagenUri },
                onVolver = { abrir(null) },
                onCrearProducto = { _, resultado -> resultado(false) },
                onEditarProducto = { id, datos, resultado ->
                    guardar({ resultado(it == null) }) {
                        val stock = listaProductos.first { it.id == id }.stock
                        actualizarProducto(catalogo.editar(sesion, id, datos, stock))
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
                modifier = Modifier.weight(1f)
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
                modifier = Modifier.weight(1f)
            )
            else -> CircularProgressIndicator(Modifier.padding(20.dp))
        }
    }
}

private fun mensajeAdministracion(fallo: Exception, porDefecto: String): String =
    when ((fallo as? HttpException)?.code()) {
        400 -> "Revisa los datos de la operación."
        401, 403 -> "Tu sesión no tiene acceso a esta operación. Vuelve a iniciar sesión."
        404 -> "El registro ya no está disponible o aún no tiene inventario."
        409 -> "El backend rechazó el cambio. Revisa el stock o el estado actual."
        else -> porDefecto
    }
