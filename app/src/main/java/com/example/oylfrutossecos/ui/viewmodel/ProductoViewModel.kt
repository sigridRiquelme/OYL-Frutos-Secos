
package com.example.oylfrutossecos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModelProvider

import com.example.oylfrutossecos.data.local.entity.ProductoEntity
import com.example.oylfrutossecos.data.repository.ProductoRepository
import com.example.oylfrutossecos.ui.validation.DatosNuevoProducto
import com.example.oylfrutossecos.ui.validation.ValidadorProducto

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class ProductoViewModel(
    private val repository: ProductoRepository
) : ViewModel() {

    // Lista observable de productos desde Room
    val productos: StateFlow<List<ProductoEntity>> =
        repository.productos.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    // NUEVA FUNCION: Crear producto
    fun crearProducto(
        datos: DatosNuevoProducto,
        onResultado: (Boolean) -> Unit
    ) {

        // Validamos los datos antes de guardar
        val errores = ValidadorProducto.validar(datos)

        if (errores.isNotEmpty()) {
            onResultado(false)
            return
        }

        // Ejecutar guardado utilizando corrutinas
        viewModelScope.launch {

            try {

                repository.crearProducto(
                    nombre = datos.nombre.trim(),
                    categoria = datos.categoria,
                    precio = datos.precio.toInt(),
                    stock = datos.stock.toInt(),
                    descripcion = datos.descripcion.trim(),
                    presentacion = datos.presentacion.trim()
                )

                // Producto guardado correctamente
                onResultado(true)

            } catch (error: Exception) {

                // Error al guardar el producto
                onResultado(false)
            }
        }
    }
}


// Factory para construir el ViewModel
class ProductoViewModelFactory(
    private val repository: ProductoRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(
                ProductoViewModel::class.java
            )
        ) {

            @Suppress("UNCHECKED_CAST")
            return ProductoViewModel(repository) as T
        }

        throw IllegalArgumentException("ViewModel desconocido")
    }
}
