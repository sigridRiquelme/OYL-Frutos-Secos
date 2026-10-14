
package com.example.oylfrutossecos.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// Representa una tienda o punto de retiro
data class PuntoRetiro(
    val nombre: String,
    val direccion: String,
    val horario: String
)

@Composable
fun MapaScreen(
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    // Ubicaciones referenciales para pruebas.
    // No representan sucursales reales de OYL.
    val puntos = listOf(
        PuntoRetiro(
            nombre = "Tienda de prueba",
            direccion = "Plaza de Armas, Santiago",
            horario = "Lunes a viernes: 10:00 a 18:00"
        ),
        PuntoRetiro(
            nombre = "Punto de retiro de prueba",
            direccion = "Parque Bustamante, Santiago",
            horario = "Lunes a sábado: 10:00 a 17:00"
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),

        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Nuestras ubicaciones",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Consulta nuestros puntos de retiro y tiendas."
        )

        // Mostramos cada ubicación
        puntos.forEach { punto ->

            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        text = punto.nombre,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = "Dirección: ${punto.direccion}"
                    )

                    Text(
                        text = "Horario: ${punto.horario}"
                    )

                    Button(
                        onClick = {

                            val direccionCodificada =
                                Uri.encode(punto.direccion)

                            val uri = Uri.parse(
                                "https://www.google.com/maps/search/?api=1&query=$direccionCodificada"
                            )

                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                uri
                            )

                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ver en Google Maps")
                    }
                }
            }
        }
    }
}
