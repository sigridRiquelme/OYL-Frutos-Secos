package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oylfrutossecos.ui.viewmodel.QrViewModel
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

@Composable
fun QRScreen(
    productos: List<ProductoCatalogo>,
    estadosProductos: Map<Int, Boolean>,
    rol: String,
    onVerProducto: (ProductoCatalogo) -> Unit,
    onAbrirInventario: (Int) -> Unit,
    modifier: Modifier = Modifier,
    qrViewModel: QrViewModel = viewModel()
) {
    val context = LocalContext.current
    val estado = qrViewModel.estado

    // El escaner lee el JSON guardado dentro del QR.
    // El ViewModel obtiene el identificador; la informacion actual viene de Room.
    val opciones = remember {
        GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    }
    val scanner = remember(context, opciones) {
        GmsBarcodeScanning.getClient(context, opciones)
    }
    val productoEncontrado = productos.firstOrNull { it.id == estado.idProducto }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Text(
            text = "Escanear producto",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Consulta la información de nuestros productos con su código QR.",
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )

        // Vista de presentacion: la camara real se abre con el boton inferior.
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF143F30))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(164.dp)
                        .border(
                            width = 2.dp,
                            color = Color(0xFFB6E5C5),
                            shape = RoundedCornerShape(17.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.QrCodeScanner,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(92.dp)
                    )
                }
                Text(
                    text = "Escanea el código QR del producto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
                Text(
                    text = "Al tocar el botón se abrirá la cámara del teléfono.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = Color(0xFFDFEEE3)
                )
            }
        }

        Button(
            onClick = {
                qrViewModel.iniciando()
                scanner.startScan()
                    .addOnSuccessListener { barcode ->
                        qrViewModel.procesar(barcode.rawValue.orEmpty(), productos)
                    }
                    .addOnCanceledListener {
                        qrViewModel.error("Escaneo cancelado.")
                    }
                    .addOnFailureListener {
                        qrViewModel.error("No se pudo abrir el escáner. Intenta nuevamente.")
                    }
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
            Spacer(modifier = Modifier.width(10.dp))
            Text("Abrir cámara y escanear")
        }

        if (productoEncontrado == null) {
            // No exponer el texto JSON ni el identificador interno al cliente.
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFEAF3EA)
            ) {
                Text(
                    text = if (estado.mensaje == "Escanea el QR de un producto.")
                        "Apunta la cámara al código QR para ver la información del producto."
                    else estado.mensaje,
                    modifier = Modifier.padding(17.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF244C34),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            val activo = estadosProductos[productoEncontrado.id] ?: productoEncontrado.activo

            Text(
                text = "Producto encontrado",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                FotoProductoOYL(
                    nombre = productoEncontrado.nombre,
                    imagenUri = productoEncontrado.imagenUri,
                    modifier = Modifier.fillMaxWidth().height(175.dp)
                )
                Column(
                    modifier = Modifier.fillMaxWidth().padding(17.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = productoEncontrado.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${productoEncontrado.categoria} · ${productoEncontrado.presentacion}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = precioOYL(productoEncontrado.precio),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = when {
                            !activo -> "No disponible para la venta"
                            productoEncontrado.stock > 0 -> "Disponible · ${productoEncontrado.stock} unidades"
                            else -> "Agotado"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (activo && productoEncontrado.stock > 0)
                            Color(0xFF286B4A) else MaterialTheme.colorScheme.error
                    )
                    HorizontalDivider()
                    if (rol == "Administrador") {
                        Button(
                            onClick = { onAbrirInventario(productoEncontrado.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Gestionar inventario")
                        }
                    } else if (activo) {
                        Button(
                            onClick = { onVerProducto(productoEncontrado) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ver detalle del producto")
                        }
                    }
                }
            }
        }
    }
}
