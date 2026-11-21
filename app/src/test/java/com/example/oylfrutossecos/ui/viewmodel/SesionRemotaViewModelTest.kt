package com.example.oylfrutossecos.ui.viewmodel

import com.example.oylfrutossecos.data.repository.*
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.*
import org.junit.Assert.*
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class SesionRemotaViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val sesion = SesionRemota(1, "Cliente", RolRemoto.CLIENTE, "jwt-prueba", 900)
    private val solicitud = SolicitudCompra("misma-clave", "Cliente", "912345678", ModalidadCompra.RETIRO, null, listOf(ItemCompra(1, 2)))
    private val pedido = PedidoRemoto("P-1", 2000, EstadoPedidoRemoto.PENDIENTE, ModalidadCompra.RETIRO)
    private val origen = mockk<PedidosRemotos>()
    private lateinit var vm: SesionRemotaViewModel
    @Before fun preparar() { Dispatchers.setMain(dispatcher); vm = SesionRemotaViewModel() }
    @After fun cerrar() { vm.cerrar(); Dispatchers.resetMain() }

    @Test fun expiraConTiempoVirtualYLimpiaLaSesion() = runTest(dispatcher) {
        vm.iniciar(sesion)
        assertSame(sesion, vm.sesion.value)
        assertEquals("Cliente", vm.nombreMostrado)
        vm.actualizarNombre(SesionRemota(1, "Otro", RolRemoto.CLIENTE, "otro", 900), "Ignorado")
        assertEquals("Cliente", vm.nombreMostrado)
        vm.actualizarNombre(sesion, "Nuevo")
        assertEquals("Nuevo", vm.nombreMostrado)
        advanceTimeBy(900_000); runCurrent()
        assertNull(vm.sesion.value); assertEquals("", vm.nombreMostrado)
    }

    @Test fun compraExitosaYLimpiezaDelResultado() = runTest(dispatcher) {
        coEvery { origen.comprar(sesion, solicitud) } returns pedido
        vm.comprar(origen, solicitud)
        coVerify(exactly = 0) { origen.comprar(any(), any()) }
        vm.iniciar(sesion)
        vm.comprar(origen)
        assertFalse(vm.enviando)
        vm.comprar(origen, solicitud)
        assertTrue(vm.enviando)
        vm.comprar(origen, solicitud)
        runCurrent()
        coVerify(exactly = 1) { origen.comprar(sesion, solicitud) }
        assertEquals(pedido, vm.pedido); assertNull(vm.solicitud); assertFalse(vm.enviando)
        vm.limpiarResultadoCompra(); assertNull(vm.pedido); assertNull(vm.errorCompra)
    }

    @Test fun erroresHttpDistinguenRechazoDeRespuestaPendiente() = runTest(dispatcher) {
        for (codigo in listOf(400, 422, 401, 403, 404, 409, 500, 503)) {
            vm.iniciar(sesion)
            coEvery { origen.comprar(any(), any()) } throws HttpException(Response.error<Any>(codigo, "{}".toResponseBody()))
            vm.comprar(origen, solicitud); runCurrent()
            assertFalse(vm.enviando)
            val esperado = when(codigo) {
                400, 422 -> "Revisa los datos del pedido."
                401, 403 -> "Tu sesión no permite realizar el pedido."
                404, 409 -> "El backend rechazó la compra. Revisa el carrito y las existencias."
                else -> "La respuesta quedó pendiente. Reintenta la misma compra."
            }
            assertEquals(esperado, vm.errorCompra)
            assertEquals(if (codigo >= 500) solicitud else null, vm.solicitud)
        }
    }

    @Test fun conexionFallidaConservaLaClaveParaReintentar() = runTest(dispatcher) {
        vm.iniciar(sesion)
        coEvery { origen.comprar(any(), any()) } throws java.io.IOException("sin conexión")
        vm.comprar(origen, solicitud); runCurrent()
        assertSame(solicitud, vm.solicitud); assertNotNull(vm.errorCompra)
        coEvery { origen.comprar(any(), any()) } returns pedido
        vm.comprar(origen); runCurrent()
        coVerify(exactly = 2) { origen.comprar(sesion, solicitud) }
        assertEquals(pedido, vm.pedido)
    }

    @Test fun cerrarCancelaCompraEnCursoSinRestaurarDatos() = runTest(dispatcher) {
        vm.iniciar(sesion)
        coEvery { origen.comprar(any(), any()) } coAnswers { awaitCancellation() }
        vm.comprar(origen, solicitud); runCurrent(); vm.cerrar(); runCurrent()
        assertNull(vm.sesion.value); assertNull(vm.pedido); assertNull(vm.errorCompra)
        assertNull(vm.solicitud); assertFalse(vm.enviando)
    }
}
