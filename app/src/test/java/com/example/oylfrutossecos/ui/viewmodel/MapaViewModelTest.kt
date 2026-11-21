package com.example.oylfrutossecos.ui.viewmodel

import org.junit.Test
import org.junit.Assert.*

class MapaViewModelTest {
    @Test fun filtrosYSeleccionConservanUnPuntoVisible() {
        val vm = MapaViewModel()
        assertEquals(4, vm.visibles.size)
        vm.filtrar("Puntos de retiro")
        assertEquals(2, vm.visibles.size)
        assertTrue(vm.seleccionado in vm.visibles)
        val segundo = vm.visibles.last()
        vm.seleccionar(segundo); vm.filtrar("Todos")
        assertEquals(segundo, vm.seleccionado)
        vm.filtrar("Tiendas")
        assertTrue(vm.visibles.all { it.tipo == "Tiendas" })
        assertEquals(vm.visibles.first(), vm.seleccionado)
        assertTrue(vm.puntos.all { it.latitud in -90.0..90.0 && it.longitud in -180.0..180.0 })
    }
}
