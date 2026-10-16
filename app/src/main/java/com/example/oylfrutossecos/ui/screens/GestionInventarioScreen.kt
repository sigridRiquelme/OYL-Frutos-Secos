package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun GestionInventarioScreen(
    productos: List<ProductoCatalogo>,
    estadosProductos: Map<Int, Boolean>,
    onVolver: () -> Unit,

    // NUEVO: Función para registrar ajustes
    onAjustarStock: (
        Int,
        String,
        Boolean,
        (String?) -> Unit
    ) -> Unit,

    modifier: Modifier = Modifier,
    productoInicialId: Int? = null,
    filtroInicial: String = "Todos"
) {

    val limiteStockBajo = 10

    var busqueda by rememberSaveable {
        mutableStateOf("")
    }

    var filtroSeleccionado by rememberSaveable(filtroInicial) {
        mutableStateOf(filtroInicial)
    }

    // NUEVO: Producto seleccionado para ajustar
    var productoEnAjuste by remember {
        mutableStateOf<ProductoCatalogo?>(null)
    }

    var cantidadAjuste by rememberSaveable {
        mutableStateOf("")
    }

    // true = Entrada; false = Salida
    var esEntrada by rememberSaveable {
        mutableStateOf(true)
    }

    var errorAjuste by remember {
        mutableStateOf<String?>(null)
    }

    var procesando by remember {
        mutableStateOf(false)
    }
// Mensaje visible después de guardar un ajuste correctamente
    var mensajeAjusteExitoso by remember {
        mutableStateOf<String?>(null)
    }
// Abrir automáticamente el ajuste del producto
// que llegó desde el escáner QR
    LaunchedEffect(productoInicialId) {

        if (productoInicialId != null) {

            val productoEncontrado = productos.firstOrNull {
                it.id == productoInicialId
            }

            if (productoEncontrado != null) {

                productoEnAjuste = productoEncontrado

                // Reiniciar formulario de ajuste
                cantidadAjuste = ""
                esEntrada = true
                errorAjuste = null
            }
        }
    }


    // Contadores del inventario
    val productosStockBajo = productos.count {
        val activo = estadosProductos[it.id] ?: true

        activo && it.stock in 1..limiteStockBajo
    }

    val productosAgotados = productos.count {
        val activo = estadosProductos[it.id] ?: true

        activo && it.stock == 0
    }

    val unidadesTotales = productos.sumOf {
        it.stock.toLong()
    }

    // Buscar y filtrar productos
    val productosFiltrados = productos.filter { producto ->

        val coincideNombre = producto.nombre.contains(
            busqueda,
            ignoreCase = true
        )

        val activo = estadosProductos[producto.id] ?: true

        val coincideFiltro = when (filtroSeleccionado) {

            "Por reponer" ->
                activo &&
                        producto.stock in 0..limiteStockBajo


            "Stock bajo" ->
                activo &&
                        producto.stock in 1..limiteStockBajo

            "Agotados" ->
                activo && producto.stock == 0

            else -> true
        }

        coincideNombre && coincideFiltro
    }

    // NUEVO: Ventana para ajustar las existencias
    productoEnAjuste?.let { producto ->

        AlertDialog(
            onDismissRequest = {
                if (!procesando) {
                    productoEnAjuste = null
                }
            },

            title = {
                Text("Ajustar inventario")
            },

            text = {

                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        text = producto.nombre,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        "Existencias actuales: ${producto.stock}"
                    )

                    Text("Tipo de movimiento")

                    // Elegir entrada o salida
                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        FilterChip(
                            selected = esEntrada,
                            onClick = {
                                esEntrada = true
                                errorAjuste = null
                            },
                            label = {
                                Text("Entrada (+)")
                            },
                            enabled = !procesando
                        )

                        FilterChip(
                            selected = !esEntrada,
                            onClick = {
                                esEntrada = false
                                errorAjuste = null
                            },
                            label = {
                                Text("Salida (-)")
                            },
                            enabled = !procesando
                        )
                    }

                    // Cantidad que queremos sumar o restar
                    OutlinedTextField(
                        value = cantidadAjuste,

                        onValueChange = {
                            cantidadAjuste = it
                            errorAjuste = null
                        },

                        label = {
                            Text("Cantidad")
                        },

                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),

                        singleLine = true,
                        isError = errorAjuste != null,
                        enabled = !procesando,
                        modifier = Modifier.fillMaxWidth(),

                        supportingText = {
                            errorAjuste?.let {
                                Text(it)
                            }
                        }
                    )

                    Text(
                        text = "Las salidas no pueden dejar " +
                                "el inventario con stock negativo.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },

            confirmButton = {

                Button(
                    onClick = {

                        procesando = true
                        errorAjuste = null

                        onAjustarStock(
                            producto.id,
                            cantidadAjuste,
                            esEntrada
                        ) { error ->

                            procesando = false


                            if (error == null) {

                                // Crear mensaje solamente después de guardar
                                val tipoMovimiento = if (esEntrada) {
                                    "Entrada"
                                } else {
                                    "Salida"
                                }

                                mensajeAjusteExitoso =
                                    "$tipoMovimiento de $cantidadAjuste unidades " +
                                            "registrada correctamente para ${producto.nombre}."

                                // Cerrar formulario y limpiar cantidad
                                productoEnAjuste = null
                                cantidadAjuste = ""

                            } else {


                                // Mostrar error y conservar formulario
                                errorAjuste = error
                            }
                        }
                    },

                    enabled = !procesando
                ) {

                    Text(
                        if (procesando)
                            "Guardando..."
                        else
                            "Guardar ajuste"
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        productoEnAjuste = null
                    },
                    enabled = !procesando
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Pantalla principal
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),

        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        TextButton(
            onClick = onVolver
        ) {
            Text("← Volver al inicio")
        }

        Text(
            text = "Gestión de Inventario",
            style = MaterialTheme.typography.headlineMedium
        )

// CONFIRMACIÓN ANIMADA DEL AJUSTE
        mensajeAjusteExitoso?.let { mensaje ->

            var iniciarAnimacion by remember(mensaje) {
                mutableStateOf(false)
            }

            LaunchedEffect(mensaje) {
                iniciarAnimacion = true
            }

            val opacidad by animateFloatAsState(
                targetValue = if (iniciarAnimacion) 1f else 0f,
                animationSpec = tween(durationMillis = 450),
                label = "Opacidad ajuste exitoso"
            )

            val desplazamiento by animateFloatAsState(
                targetValue = if (iniciarAnimacion) 0f else -25f,
                animationSpec = tween(durationMillis = 450),
                label = "Desplazamiento ajuste exitoso"
            )

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = opacidad
                        translationY = desplazamiento
                    }
            ) {

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        text = "✓ Inventario actualizado",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = mensaje,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    TextButton(
                        onClick = {
                            mensajeAjusteExitoso = null
                        }
                    ) {
                        Text("Entendido")
                    }
                }
            }
        }

        // Resumen
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text = "Resumen del inventario",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    "Productos registrados: ${productos.size}"
                )

                Text(
                    "Unidades totales: $unidadesTotales"
                )

                Text(
                    "Productos con stock bajo: $productosStockBajo"
                )

                Text(
                    "Productos agotados: $productosAgotados"
                )

                Text(
                    "Stock bajo: entre 1 y " +
                            "$limiteStockBajo unidades.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Buscador
        OutlinedTextField(
            value = busqueda,

            onValueChange = {
                busqueda = it
            },

            label = {
                Text("Buscar producto")
            },

            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Filtros
        Row(
            modifier = Modifier.horizontalScroll(
                rememberScrollState()
            ),

            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {


            listOf(
                "Todos",
                "Por reponer",
                "Stock bajo",
                "Agotados"
            )

            .forEach { filtro ->

                FilterChip(
                    selected = filtroSeleccionado == filtro,

                    onClick = {
                        filtroSeleccionado = filtro
                    },

                    label = {
                        Text(filtro)
                    }
                )
            }
        }

        // Listado
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (productosFiltrados.isEmpty()) {

                item {
                    Text(
                        text = "No se encontraron productos.",
                        modifier = Modifier.padding(16.dp)
                    )
                }

            } else {

                items(
                    items = productosFiltrados,
                    key = { it.id }
                ) { producto ->

                    val activo =
                        estadosProductos[producto.id] ?: true

                    val agotado = producto.stock == 0

                    val stockBajo =
                        producto.stock in 1..limiteStockBajo

                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp),

                            verticalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            Text(
                                text = producto.nombre,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                "Categoría: ${producto.categoria}"
                            )

                            Text(
                                "Existencias: ${producto.stock} unidades",
                                style = MaterialTheme.typography.titleSmall
                            )

                            Text(
                                if (activo)
                                    "Producto activo"
                                else
                                    "Producto inactivo"
                            )

                            HorizontalDivider()

                            // Indicador de existencias
                            Text(
                                text = when {
                                    !activo -> "Producto inactivo"

                                    agotado -> "Sin existencias"

                                    stockBajo -> "Stock bajo"

                                    else -> "Stock suficiente"
                                },

                                color = when {
                                    !activo ->
                                        MaterialTheme.colorScheme.onSurfaceVariant

                                    agotado || stockBajo ->
                                        MaterialTheme.colorScheme.error

                                    else ->
                                        MaterialTheme.colorScheme.primary
                                }
                            )

                            // NUEVO: Botón para modificar stock
                            Button(
                                onClick = {

                                    productoEnAjuste = producto

                                    // Reiniciar datos del formulario
                                    cantidadAjuste = ""
                                    esEntrada = true
                                    errorAjuste = null
                                },

                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Ajustar stock")
                            }
                        }
                    }
                }
            }
        }
    }
}
