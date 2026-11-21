package com.example.oylfrutossecos.data.local

import android.content.Context
import com.example.oylfrutossecos.feature.pedidos.data.PedidosRepositoryLocal
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class PedidoLocalStoreTest {
    private val contexto: Context get() = RuntimeEnvironment.getApplication()
    private val productos = listOf(DetallePedidoLocal(1, "Nueces", 2, 2500))
    @Before fun limpiar() { contexto.getSharedPreferences("pedidos_oyl", 0).edit().clear().commit() }
    @Test fun persistePedidosYDetallesSeparandoClienteYContador() {
        assertTrue(PedidosRepositoryLocal.cargar(contexto).isEmpty())
        val primero = PedidosRepositoryLocal.registrar(contexto, 7, "Cliente", "912345678", "Retiro", "", productos)
        val segundo = PedidosRepositoryLocal.registrar(contexto, null, "Invitado", "912345678", "Despacho", "Santiago", productos)
        assertEquals("OYL-P-0001", primero.numero); assertEquals("OYL-P-0002", segundo.numero)
        assertEquals(5000, primero.total); assertEquals(7, primero.clienteId); assertNull(segundo.clienteId)
        assertEquals(listOf(primero, segundo), PedidosRepositoryLocal.cargar(contexto))
        val actualizados = PedidosRepositoryLocal.actualizarEstado(contexto, primero.numero, "En preparación")
        assertEquals("En preparación", actualizados.first().estado)
        assertEquals(segundo, actualizados.last())
        assertEquals(actualizados, PedidosRepositoryLocal.cargar(contexto))
    }
    @Test fun rechazaModalidadInvalidaPedidosVaciosYTransicionesIlegales() {
        assertThrows(IllegalArgumentException::class.java) {
            PedidosRepositoryLocal.registrar(contexto, 7, "Cliente", "912345678", "Otra", "", productos)
        }
        assertThrows(IllegalArgumentException::class.java) {
            PedidosRepositoryLocal.registrar(contexto, 7, "Cliente", "912345678", "Retiro", "", emptyList())
        }
        assertThrows(IllegalArgumentException::class.java) {
            PedidosRepositoryLocal.actualizarEstado(contexto, "no-existe", "Entregado")
        }
        val pedido = PedidosRepositoryLocal.registrar(contexto, 7, "Cliente", "912345678", "Retiro", "", productos)
        assertThrows(IllegalArgumentException::class.java) { PedidosRepositoryLocal.actualizarEstado(contexto, pedido.numero, "En despacho") }
        assertEquals("Pendiente", PedidosRepositoryLocal.cargar(contexto).single().estado)
    }
}
