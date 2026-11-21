package com.example.oylfrutossecos.feature.pedidos.data

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class GeocodificadorDespachoTest {
    @Test fun configuracionPredeterminadaNoConsultaDireccionesExternas() = runBlocking {
        assertEquals(UbicacionDespacho.Deshabilitada,
            GeocodificadorDespacho.buscar(RuntimeEnvironment.getApplication(), DireccionDemo.MONEDA))
    }
}
