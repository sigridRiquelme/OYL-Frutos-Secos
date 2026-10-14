
package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode

@Composable
fun QRScreen(
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    var codigoLeido by remember {
        mutableStateOf("")
    }

    var mensaje by remember {
        mutableStateOf("Presiona el botón para escanear.")
    }

    // Configurar lectura de códigos QR
    val opciones = remember {
        GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    }

    val scanner = remember(context) {
        GmsBarcodeScanning.getClient(context, opciones)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Text(
            text = "Escáner QR",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Escanea el código QR de un producto OYL."
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {

                scanner.startScan()
                    .addOnSuccessListener { barcode ->

                        codigoLeido =
                            barcode.rawValue?.trim().orEmpty()

                        val codigoValido =
                            Regex("OYL-[1-6]")
                                .matches(codigoLeido)

                        mensaje = if (codigoValido) {
                            "Código de producto OYL reconocido."
                        } else {
                            "QR leído, pero no corresponde a un producto OYL válido."
                        }
                    }

                    .addOnCanceledListener {
                        mensaje = "Escaneo cancelado."
                    }

                    .addOnFailureListener { error ->
                        mensaje = "Error al escanear: ${error.localizedMessage}"
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

        Text(
            text = mensaje
        )
    }
}
