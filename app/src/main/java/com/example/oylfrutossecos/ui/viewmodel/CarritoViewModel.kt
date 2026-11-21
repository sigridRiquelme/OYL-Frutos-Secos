
package com.example.oylfrutossecos.ui.viewmodel

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import com.example.oylfrutossecos.data.model.ProductoCatalogo
import com.example.oylfrutossecos.feature.carrito.domain.ReglasCarrito

class CarritoViewModel : ViewModel() {

    // Producto ID -> cantidad
    val cantidades = mutableStateMapOf<Int, Int>()

    // Producto ID -> información del producto
    val productos = mutableStateMapOf<Int, ProductoCatalogo>()

    // Agregar un producto al carrito
    fun agregar(
        producto: ProductoCatalogo,
        cantidad: Int
    ): Boolean {

        val actual = cantidades[producto.id] ?: 0

        if (!ReglasCarrito.puedeAgregar(
                producto = producto,
                cantidadSolicitada = cantidad,
                cantidadActual = actual
            )
        ) {
            return false
        }

        cantidades[producto.id] = actual + cantidad
        productos[producto.id] = producto

        return true
    }

    // Cambiar la cantidad de un producto
    fun cambiarCantidad(
        producto: ProductoCatalogo?,
        id: Int,
        nuevaCantidad: Int
    ) {

        val anterior = cantidades[id] ?: 0

        // Cantidad cero o negativa: eliminar producto
        if (nuevaCantidad <= 0) {

            cantidades.remove(id)
            productos.remove(id)

        } else if (ReglasCarrito.puedeCambiarCantidad(
                producto = producto,
                cantidadAnterior = anterior,
                nuevaCantidad = nuevaCantidad
            )
        ) {

            producto?.let { productoValido ->
                cantidades[id] = nuevaCantidad
                productos[id] = productoValido
            }
        }
    }

    // Vaciar completamente el carrito
    fun vaciar() {
        cantidades.clear()
        productos.clear()
    }
}
