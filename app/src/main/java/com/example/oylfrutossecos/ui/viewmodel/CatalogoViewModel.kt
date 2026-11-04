package com.example.oylfrutossecos.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.oylfrutossecos.ui.screens.ProductoCatalogo

// Un ViewModel por responsabilidad: mantiene la busqueda y los filtros del catalogo.
class CatalogoViewModel : ViewModel() {

    private val _busqueda = MutableStateFlow("")
    val busqueda = _busqueda.asStateFlow()

    private val _categoria = MutableStateFlow("Todos")
    val categoria = _categoria.asStateFlow()

    fun cambiarBusqueda(texto: String) {
        _busqueda.value = texto
    }

    fun cambiarCategoria(nuevaCategoria: String) {
        _categoria.value = nuevaCategoria
    }

    fun filtrar(productos: List<ProductoCatalogo>): List<ProductoCatalogo> {
        return productos.filter { producto ->
            producto.activo &&
                producto.nombre.contains(_busqueda.value.trim(), ignoreCase = true) &&
                (_categoria.value == "Todos" || producto.categoria == _categoria.value)
        }
    }
}
