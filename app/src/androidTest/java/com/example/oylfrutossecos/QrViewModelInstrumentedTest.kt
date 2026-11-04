package com.example.oylfrutossecos

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.oylfrutossecos.ui.screens.ProductoCatalogo
import com.example.oylfrutossecos.ui.viewmodel.QrViewModel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

// Comprueba que el texto obtenido del QR sea un JSON.
// No existe una clase adicional para interpretar el codigo del producto.
@RunWith(AndroidJUnit4::class)
class QrViewModelInstrumentedTest {

    private val productos = listOf(
        ProductoCatalogo(id = 1, nombre = "Almendras", categoria = "Frutos secos",
            precio = 5990, stock = 25)
    )

    @Test
    fun codigoJsonCorrectoReconoceProducto() {
        val vm = QrViewModel()
        vm.procesar("""{"codigo":"OYL-1","idProducto":1,"nombre":"Almendras"}""", productos)
        assertEquals(1, vm.estado.idProducto)
        assertEquals("OYL-1", vm.estado.codigoLeido)
    }

    @Test
    fun codigoJsonSoloConCodigoReconoceProducto() {
        val vm = QrViewModel()
        vm.procesar("""{"codigo":"OYL-1"}""", productos)
        assertEquals(1, vm.estado.idProducto)
    }

    @Test
    fun textoSinJsonSeRechaza() {
        val vm = QrViewModel()
        vm.procesar("OYL-1", productos)
        assertNull(vm.estado.idProducto)
    }

    @Test
    fun identificadoresInconsistentesSeRechazan() {
        val vm = QrViewModel()
        vm.procesar("""{"codigo":"OYL-2","idProducto":1}""", productos)
        assertNull(vm.estado.idProducto)
    }

    @Test
    fun productoInexistenteSeRechaza() {
        val vm = QrViewModel()
        vm.procesar("""{"codigo":"OYL-999","idProducto":999}""", productos)
        assertNull(vm.estado.idProducto)
    }
}
