package com.example.oylfrutossecos.ui.viewmodel

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import com.example.oylfrutossecos.ui.screens.ProductoCatalogo

// Estado del carrito separado del Dashboard. Una instancia por cliente autenticado.
class CarritoViewModel : ViewModel() {

    // productoId -> unidades
    val cantidades = mutableStateMapOf<Int, Int>()

    // productoId -> ficha del producto vista al agregar
    val productos = mutableStateMapOf<Int, ProductoCatalogo>()

    fun agregar(producto: ProductoCatalogo, cantidad: Int): Boolean {
        val actual = cantidades[producto.id] ?: 0
        val disponible = (producto.stock - actual).coerceAtLeast(0)
        if (!producto.activo || cantidad !in 1..disponible) return false
        cantidades[producto.id] = actual + cantidad
        productos[producto.id] = producto
        return true
    }

    fun cambiarCantidad(producto: ProductoCatalogo?, id: Int, nuevaCantidad: Int) {
        val anterior = cantidades[id] ?: 0
        if (nuevaCantidad <= 0) {
            cantidades.remove(id)
            productos.remove(id)
        } else if (producto != null && producto.activo &&
            (nuevaCantidad < anterior || nuevaCantidad <= producto.stock)) {
            cantidades[id] = nuevaCantidad
            productos[id] = producto
        }
    }

    fun vaciar() {
        cantidades.clear()
        productos.clear()
    }
}
