
package com.example.oylfrutossecos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oylfrutossecos.data.local.entity.ProductoEntity
import com.example.oylfrutossecos.data.repository.ProductoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.ViewModelProvider

class ProductoViewModel(
    private val repository: ProductoRepository
) : ViewModel() {

    // Lista observable de productos
    val productos: StateFlow<List<ProductoEntity>> =
        repository.productos.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}


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
