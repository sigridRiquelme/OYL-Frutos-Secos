package com.example.oylfrutossecos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.oylfrutossecos.data.repository.PedidoRemoto
import com.example.oylfrutossecos.data.repository.PedidosRemotos
import com.example.oylfrutossecos.data.repository.SesionRemota
import com.example.oylfrutossecos.data.repository.SolicitudCompra
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import kotlin.time.Duration.Companion.seconds

class SesionRemotaViewModel : ViewModel() {
    private val _sesion = MutableStateFlow<SesionRemota?>(null)
    val sesion = _sesion.asStateFlow()
    private var vencimiento: Job? = null
    private var envio: Job? = null
    internal var nombreMostrado by mutableStateOf("")
        private set
    internal val carrito = CarritoViewModel()
    internal var solicitud by mutableStateOf<SolicitudCompra?>(null)
        private set
    internal var enviando by mutableStateOf(false)
        private set
    internal var pedido by mutableStateOf<PedidoRemoto?>(null)
        private set
    internal var errorCompra by mutableStateOf<String?>(null)
        private set

    fun iniciar(sesion: SesionRemota) {
        cerrar()
        nombreMostrado = sesion.nombre
        _sesion.value = sesion
        vencimiento = viewModelScope.launch {
            delay(sesion.expiraEnSegundos.seconds)
            cerrar()
        }
    }

    fun cerrar() {
        vencimiento?.cancel()
        vencimiento = null
        _sesion.value = null
        nombreMostrado = ""
        envio?.cancel()
        envio = null
        solicitud = null
        enviando = false
        pedido = null
        errorCompra = null
        carrito.vaciar()
    }

    internal fun comprar(origen: PedidosRemotos, nuevaSolicitud: SolicitudCompra? = null) {
        val sesionActual = _sesion.value ?: return
        if (enviando) return
        val pendiente = solicitud ?: nuevaSolicitud ?: return
        solicitud = pendiente
        errorCompra = null
        enviando = true
        envio = viewModelScope.launch {
            try {
                pedido = origen.comprar(sesionActual, pendiente)
                solicitud = null
                carrito.vaciar()
            } catch (cancelada: CancellationException) {
                throw cancelada
            } catch (error: HttpException) {
                errorCompra = when (error.code()) {
                    400, 422 -> "Revisa los datos del pedido."
                    401, 403 -> "Tu sesión no permite realizar el pedido."
                    404, 409 -> "El backend rechazó la compra. Revisa el carrito y las existencias."
                    else -> "La respuesta quedó pendiente. Reintenta la misma compra."
                }
                if (error.code() in setOf(400, 401, 403, 404, 409, 422)) solicitud = null
            } catch (_: Exception) {
                errorCompra = "No pudimos confirmar la respuesta. Reintenta la misma compra."
            } finally {
                if (_sesion.value === sesionActual) enviando = false
            }
        }
    }

    internal fun limpiarResultadoCompra() {
        pedido = null
        errorCompra = null
    }

    internal fun actualizarNombre(sesion: SesionRemota, nombre: String) {
        if (_sesion.value === sesion) nombreMostrado = nombre
    }
}
