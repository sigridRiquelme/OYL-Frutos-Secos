package com.example.oylfrutossecos.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

// Ubicaciones academicas de demostracion, NO locales confirmados de la empresa.
data class PuntoMapaOyl(
    val id: Int,
    val nombre: String,
    val direccion: String,
    val horario: String,
    val tipo: String,
    val latitud: Double,
    val longitud: Double
)

class MapaViewModel : ViewModel() {
    val puntos = listOf(
        PuntoMapaOyl(1, "Tienda demo - Santiago Centro", "Plaza de Armas, Santiago",
            "Lun a vie 10:00 - 18:00", "Tiendas", -33.4372, -70.6506),
        PuntoMapaOyl(2, "Punto demo - Parque Bustamante", "Parque Bustamante, Providencia",
            "Lun a sab 10:00 - 17:00", "Puntos de retiro", -33.4482, -70.6315),
        PuntoMapaOyl(3, "Punto demo - Los Leones", "Metro Los Leones, Providencia",
            "Lun a vie 10:00 - 19:00", "Puntos de retiro", -33.4196, -70.6107),
        PuntoMapaOyl(4, "Tienda demo - Parque O'Higgins", "Parque O'Higgins, Santiago",
            "Sab 10:00 - 16:00", "Tiendas", -33.4638, -70.6556)
    )

    var filtro by mutableStateOf("Todos")
        private set
    var seleccionado by mutableStateOf(puntos.first())
        private set

    val visibles: List<PuntoMapaOyl>
        get() = if (filtro == "Todos") puntos else puntos.filter { it.tipo == filtro }

    fun filtrar(tipo: String) {
        filtro = tipo
        if (seleccionado !in visibles) seleccionado = visibles.first()
    }

    fun seleccionar(punto: PuntoMapaOyl) {
        seleccionado = punto
    }
}
