package com.example.oylfrutossecos.ui.viewmodel

import com.example.oylfrutossecos.data.repository.*
import com.example.oylfrutossecos.data.model.ProductoCatalogo
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class FactoriesYCarritoTest {
    @Before fun preparar() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun cerrar() { Dispatchers.resetMain() }
    @Test fun factoriesCreanSoloSuTipoDeViewModel() {
        val productos = mockk<ProductoRepository>()
        every { productos.productos } returns flowOf(emptyList())
        val productoFactory = ProductoViewModelFactory(productos)
        val clienteFactory = ClienteViewModelFactory(mockk())
        assertTrue(productoFactory.create(ProductoViewModel::class.java) is ProductoViewModel)
        assertTrue(clienteFactory.create(ClienteViewModel::class.java) is ClienteViewModel)
        assertThrows(IllegalArgumentException::class.java) { productoFactory.create(QrViewModel::class.java) }
        assertThrows(IllegalArgumentException::class.java) { clienteFactory.create(QrViewModel::class.java) }
    }
    @Test fun carritoActualizaSoloCantidadesPermitidas() {
        val vm = CarritoViewModel()
        val producto = ProductoCatalogo(1, "Nueces", "Frutos secos", 2500, 8)
        assertTrue(vm.agregar(producto, 2))
        vm.cambiarCantidad(producto, 1, 4)
        assertEquals(4, vm.cantidades[1]); assertEquals(producto, vm.productos[1])
        vm.cambiarCantidad(producto, 1, 9); assertEquals(4, vm.cantidades[1])
        vm.cambiarCantidad(null, 1, 3); assertEquals(4, vm.cantidades[1])
        vm.cambiarCantidad(producto, 1, -1); assertFalse(vm.productos.containsKey(1))
        vm.cambiarCantidad(null, 999, 1); assertFalse(vm.cantidades.containsKey(999))
    }
}
