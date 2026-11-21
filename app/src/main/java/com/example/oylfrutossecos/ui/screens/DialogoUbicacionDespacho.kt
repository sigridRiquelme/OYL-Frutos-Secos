package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.oylfrutossecos.feature.pedidos.data.DireccionDemo
import com.example.oylfrutossecos.feature.pedidos.data.GeocodificadorDespacho
import com.example.oylfrutossecos.feature.pedidos.data.UbicacionDespacho

@Composable
internal fun DialogoUbicacionDespacho(direccion: String, onVolver: () -> Unit) {
    val demo = DireccionDemo.desde(direccion)
    val contexto = LocalContext.current.applicationContext
    var estado by remember(demo) { mutableStateOf<UbicacionDespacho>(UbicacionDespacho.Cargando) }
    var consultar by remember(demo) { mutableStateOf(false) }

    LaunchedEffect(demo, consultar) {
        if (demo != null && consultar) estado = GeocodificadorDespacho.buscar(contexto, demo)
    }

    AlertDialog(
        onDismissRequest = onVolver,
        title = { Text("Ubicación del despacho") },
        text = {
            when {
                demo == null -> Text(
                    "La búsqueda externa solo admite lugares públicos de demostración. " +
                        "Usa Palacio de La Moneda, Santiago, Chile."
                )
                !consultar -> Text("Se consultará este lugar público de demostración en Nominatim: ${demo.consulta}")
                else -> ContenidoUbicacionDespacho(estado)
            }
        },
        confirmButton = {
            if (demo != null && !consultar) {
                TextButton(onClick = { consultar = true }) { Text("Consultar ubicación") }
            } else {
                TextButton(onClick = onVolver) { Text("Volver al pedido") }
            }
        },
        dismissButton = {
            if (demo != null && !consultar) {
                TextButton(onClick = onVolver) { Text("Volver al pedido") }
            }
        }
    )
}

@Composable
internal fun ContenidoUbicacionDespacho(estado: UbicacionDespacho) {
    when (estado) {
        UbicacionDespacho.Cargando -> Column {
            Text("Buscando ubicación…")
            LinearProgressIndicator()
        }
        is UbicacionDespacho.Encontrada ->
            MapaDespacho(estado, Modifier.heightIn(max = 370.dp))
        UbicacionDespacho.NoEncontrada -> Text("No se encontró la dirección en OpenStreetMap.")
        UbicacionDespacho.Ambigua -> Text("La coincidencia es imprecisa. Verifica el nombre y la comuna.")
        UbicacionDespacho.SinConexion -> Text("No se pudo conectar. Revisa Internet y vuelve a abrir la ubicación.")
        UbicacionDespacho.RespuestaInvalida -> Text("El servicio devolvió una respuesta que no se pudo verificar.")
        UbicacionDespacho.Deshabilitada -> Text("La geocodificación está deshabilitada en esta instalación.")
        is UbicacionDespacho.ErrorHttp -> Text("El servicio de geocodificación respondió con un error HTTP ${estado.codigo}.")
    }
}
