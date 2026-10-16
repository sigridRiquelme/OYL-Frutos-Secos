
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

    // Observar productos almacenados en Room
    val productos: StateFlow<List<ProductoEntity>> =
        repository.productos.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Crear nuevo producto
    fun crearProducto(
        datos: DatosNuevoProducto,
        onResultado: (Boolean) -> Unit
    ) {

        val errores = ValidadorProducto.validar(datos)

        if (errores.isNotEmpty()) {
            onResultado(false)
            return
        }

        viewModelScope.launch {

            try {

                repository.crearProducto(
                    nombre = datos.nombre.trim(),
                    categoria = datos.categoria,
                    precio = datos.precio.toInt(),
                    stock = datos.stock.toInt(),
                    descripcion = datos.descripcion.trim(),
                    presentacion = datos.presentacion.trim(),

                    // NUEVO: Enviar fotografía a Room
                    imagenUri = datos.imagenUri
                )

                onResultado(true)

            } catch (error: Exception) {
                onResultado(false)
            }
        }
    }

    // Editar producto existente
    fun editarProducto(
        id: Int,
        datos: DatosNuevoProducto,
        onResultado: (Boolean) -> Unit
    ) {

        val errores = ValidadorProducto.validar(datos)

        if (errores.isNotEmpty()) {
            onResultado(false)
            return
        }

        viewModelScope.launch {

            try {

                repository.editarProducto(
                    id = id,
                    nombre = datos.nombre.trim(),
                    categoria = datos.categoria,
                    precio = datos.precio.toInt(),
                    stock = datos.stock.toInt(),
                    descripcion = datos.descripcion.trim(),
                    presentacion = datos.presentacion.trim(),

                    // NUEVO: Guardar la fotografía editada
                    imagenUri = datos.imagenUri
                )

                onResultado(true)

            } catch (error: Exception) {
                onResultado(false)
            }
        }
    }

    // Activar o desactivar producto
    fun cambiarEstadoProducto(
        id: Int,
        activo: Boolean,
        onResultado: (Boolean) -> Unit
    ) {

        viewModelScope.launch {

            try {

                repository.cambiarEstadoProducto(
                    id = id,
                    activo = activo
                )

                onResultado(true)

            } catch (error: Exception) {
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

        if (
            modelClass.isAssignableFrom(
                ProductoViewModel::class.java
            )
        ) {

            @Suppress("UNCHECKED_CAST")
            return ProductoViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido"
        )
    }
}
