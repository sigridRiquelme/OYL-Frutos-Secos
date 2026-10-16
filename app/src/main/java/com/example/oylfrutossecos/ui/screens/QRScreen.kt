
package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode

@Composable
fun QRScreen(
    productos: List<ProductoCatalogo>,
    estadosProductos: Map<Int, Boolean>,
    rol: String,
    onVerProducto: (ProductoCatalogo) -> Unit,
    onAbrirInventario: (Int) -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    var codigoLeido by rememberSaveable {
        mutableStateOf("")
    }

    var idProductoLeido by rememberSaveable {
        mutableStateOf<Int?>(null)
    }

    var mensaje by remember {
        mutableStateOf("Presiona el botón para escanear.")
    }

    // Permite reconocer cualquier ID positivo:
    // OYL-1, OYL-7, OYL-15, etc.
    val patronQR = remember {
        Regex(
            "^OYL-([1-9][0-9]*)$",
            RegexOption.IGNORE_CASE
        )
    }

    val opciones = remember {
        GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    }

    val scanner = remember(context) {
        GmsBarcodeScanning.getClient(context, opciones)
    }

    // Buscar el producto REAL en la lista de Room
    val productoEncontrado = productos.firstOrNull {
        it.id == idProductoLeido
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Escáner QR",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Escanea un producto OYL para consultar sus datos."
        )

        Button(
            onClick = {

                codigoLeido = ""
                idProductoLeido = null
                mensaje = "Abriendo escáner..."

                scanner.startScan()
                    .addOnSuccessListener { barcode ->

                        codigoLeido =
                            barcode.rawValue?.trim().orEmpty()

                        // Comprobar el formato del código
                        val coincidencia =
                            patronQR.matchEntire(codigoLeido)

                        val id = coincidencia
                            ?.groupValues
                            ?.getOrNull(1)
                            ?.toIntOrNull()

                        if (id == null) {

                            mensaje =
                                "Código QR no válido para OYL."

                        } else {

                            // Comprobar que existe en Room
                            val producto = productos.firstOrNull {
                                it.id == id
                            }

                            if (producto == null) {

                                mensaje =
                                    "No existe un producto con ID $id."

                            } else {

                                idProductoLeido = id

                                mensaje =
                                    "Producto identificado correctamente."
                            }
                        }
                    }
                    .addOnCanceledListener {
                        mensaje = "Escaneo cancelado."
                    }
                    .addOnFailureListener { error ->
                        mensaje =
                            "Error al escanear: ${error.localizedMessage}"
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Abrir escáner QR")
        }

        HorizontalDivider()

        Text(
            text = "Resultado del escaneo",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = if (codigoLeido.isEmpty()) {
                "Todavía no se ha leído ningún código."
            } else {
                codigoLeido
            }
        )

        Text(mensaje)

        // Mostrar ficha breve si existe el producto
        productoEncontrado?.let { producto ->

            val activo =
                estadosProductos[producto.id] ?: true

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
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text("ID: ${producto.id}")

                    Text("Categoría: ${producto.categoria}")

                    Text("Precio: $${producto.precio}")

                    Text("Stock: ${producto.stock} unidades")

                    Text(
                        if (activo) {
                            "Estado: Activo"
                        } else {
                            "Estado: Inactivo"
                        }
                    )

                    HorizontalDivider()

                    if (rol == "Administrador") {

                        Button(
                            onClick = {
                                onAbrirInventario(producto.id)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Abrir Gestión de Inventario")
                        }

                    } else if (activo) {

                        Button(
                            onClick = {
                                onVerProducto(producto)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ver detalle del producto")
                        }

                    } else {

                        Text(
                            text = "Producto no disponible para la venta.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
