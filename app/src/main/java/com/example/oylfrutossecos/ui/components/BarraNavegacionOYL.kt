
package com.example.oylfrutossecos.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Person

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import com.example.oylfrutossecos.feature.navegacion.domain.ReglasNavegacionDashboard

@Composable
fun BarraNavegacionOYL(
    pantallaActual: String,
    onSeleccionar: (String) -> Unit
) {

    val opciones = listOf(
        "Inicio" to Icons.Filled.Home,
        "Mapa" to Icons.Filled.Place,
        "QR" to Icons.Filled.QrCodeScanner,
        "Pedidos" to Icons.Filled.ReceiptLong,
        "Perfil" to Icons.Filled.Person
    )

    NavigationBar {

        opciones.forEach { (nombre, icono) ->

            NavigationBarItem(

                selected =
                    ReglasNavegacionDashboard.estaSeleccionada(
                        opcion = nombre,
                        pantallaActual = pantallaActual
                    ),

                onClick = {
                    onSeleccionar(nombre)
                },

                icon = {
                    Icon(
                        imageVector = icono,
                        contentDescription = nombre
                    )
                },

                label = {
                    Text(nombre)
                }
            )
        }
    }
}
