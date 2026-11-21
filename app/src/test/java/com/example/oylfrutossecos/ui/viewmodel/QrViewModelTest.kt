package com.example.oylfrutossecos.ui.viewmodel

import com.example.oylfrutossecos.data.model.ProductoCatalogo
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class QrViewModelTest {
    private val productos = listOf(ProductoCatalogo(1, "Nueces", "Frutos secos", 2500, 8))
    @Test fun identificaAliasesSinConfiarEnPrecioOStockDelQr() {
        val vm = QrViewModel()
        assertNull(vm.estado.idProducto)
        vm.iniciando(); assertEquals("Abriendo escáner...", vm.estado.mensaje)
        for (json in listOf(
            """{"codigo":"OYL-1"}""",
            """{"codigo":"oyl-1","idProducto":1,"nombre":"Referencia","precio":1,"stock":9999}""",
            """{"id":1,"nombreReferencia":"Referencia"}""",
            """{"idProducto":1,"nombre":""}"""
        )) {
            vm.procesar("  $json  ", productos)
            assertEquals(1, vm.estado.idProducto)
            assertTrue(vm.estado.codigoLeido.equals("OYL-1", true))
            assertEquals("Producto identificado correctamente.", vm.estado.mensaje)
            assertTrue(vm.estado.nombreReferencia in listOf("Nueces", "Referencia"))
        }
        val anterior = vm.estado
        vm.error("Cancelado"); assertEquals(anterior.copy(mensaje = "Cancelado"), vm.estado)
    }
    @Test fun rechazaCodigosInvalidosJsonCorruptoEIdentidadesIncoherentes() {
        val vm = QrViewModel()
        for (texto in listOf("", "https://ejemplo.test", "{" + "a".repeat(2048), "{mal", "{}",
            """{"idProducto":0}""", """{"idProducto":-1}""", """{"codigo":"OYL-2","idProducto":1}""",
            """{"codigo":"OYL-999999999999999999999999"}""")) {
            vm.procesar(texto, productos)
            assertNull("Aceptó $texto", vm.estado.idProducto)
            assertNotEquals("Producto identificado correctamente.", vm.estado.mensaje)
        }
        vm.procesar("""{"codigo":"OYL-2"}""", productos)
        assertEquals("OYL-2", vm.estado.codigoLeido)
        assertEquals("No encontramos este producto en el catálogo.", vm.estado.mensaje)
    }
}
