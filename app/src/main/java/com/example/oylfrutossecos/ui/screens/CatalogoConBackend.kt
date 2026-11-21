package com.example.oylfrutossecos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.oylfrutossecos.data.repository.CatalogoRemoto
import kotlinx.coroutines.CancellationException

@Composable
internal fun CatalogoConBackend(
    host: String,
    rol: String,
    modifier: Modifier = Modifier,
    cantidadesCarrito: Map<Int, Int> = emptyMap(),
    agregarAlCarrito: ((ProductoCatalogo, Int) -> Unit)? = null
) {
    val origen = remember(host) { CatalogoRemoto(host) }
    var productos by remember(origen) { mutableStateOf<List<ProductoCatalogo>?>(null) }
    var detalle by remember(origen) { mutableStateOf<ProductoCatalogo?>(null) }
    var error by remember(origen) { mutableStateOf(false) }
    var intento by remember(origen) { mutableIntStateOf(0) }

    LaunchedEffect(origen, intento) {
        try {
            productos = origen.cargar()
        } catch (cancelada: CancellationException) {
            throw cancelada
        } catch (_: Exception) {
            error = true
        }
    }

    val seleccionado = detalle
    val resultado = productos
    BackHandler(enabled = seleccionado != null) { detalle = null }
    when {
        seleccionado != null -> DetalleProductoScreen(
            producto = seleccionado,
            rol = rol,
            cantidadYaAgregada = cantidadesCarrito[seleccionado.id] ?: 0,
            onVolver = { detalle = null },
            onAgregar = { agregarAlCarrito?.invoke(seleccionado, it) },
            modifier = modifier,
            permiteAgregar = agregarAlCarrito != null
        )
        resultado != null -> CatalogoScreen(resultado, modifier) { detalle = it }
        else -> Column(
            modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (error) {
                Text("No pudimos cargar el catálogo.")
                Button(onClick = {
                    error = false
                    intento++
                }) { Text("Reintentar") }
            } else {
                CircularProgressIndicator()
                Text("Cargando catálogo…")
            }
        }
    }
}
