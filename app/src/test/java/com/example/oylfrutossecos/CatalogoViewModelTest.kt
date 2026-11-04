package com.example.oylfrutossecos

import com.example.oylfrutossecos.ui.screens.ProductoCatalogo
import com.example.oylfrutossecos.ui.viewmodel.CatalogoViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class CatalogoViewModelTest {
    @Test fun buscarYFiltrarPorCategoria() {
        val vm = CatalogoViewModel()
        val productos = listOf(
            ProductoCatalogo(1, "Almendras", "Frutos secos", 5990, 20),
            ProductoCatalogo(2, "Nueces", "Frutos secos", 6490, 15),
            ProductoCatalogo(3, "Semillas de chia", "Semillas", 2990, 10),
            ProductoCatalogo(4, "Pasas", "Deshidratados", 1990, 10, activo = false)
        )
        assertEquals(3, vm.filtrar(productos).size)
        vm.cambiarCategoria("Frutos secos")
        assertEquals(2, vm.filtrar(productos).size)
        vm.cambiarBusqueda("alm")
        assertEquals(listOf(1), vm.filtrar(productos).map { it.id })
    }
}
