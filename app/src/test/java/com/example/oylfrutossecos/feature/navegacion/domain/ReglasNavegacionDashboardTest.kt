
package com.example.oylfrutossecos.feature.navegacion.domain

import io.kotest.matchers.shouldBe
import org.junit.Test

class ReglasNavegacionDashboardTest {

    // PRUEBA 1
    @Test
    fun inicioApareceSeleccionado() {
        ReglasNavegacionDashboard.estaSeleccionada(
            "Inicio", "Inicio"
        ) shouldBe true
    }

    // PRUEBA 2
    @Test
    fun mapaApareceSeleccionado() {
        ReglasNavegacionDashboard.estaSeleccionada(
            "Mapa", "Mapa"
        ) shouldBe true
    }

    // PRUEBA 3
    @Test
    fun qrApareceSeleccionado() {
        ReglasNavegacionDashboard.estaSeleccionada(
            "QR", "QR"
        ) shouldBe true
    }

    // PRUEBA 4
    @Test
    fun pedidosApareceSeleccionado() {
        ReglasNavegacionDashboard.estaSeleccionada(
            "Pedidos", "Pedidos"
        ) shouldBe true
    }

    // PRUEBA 5
    @Test
    fun perfilApareceSeleccionado() {
        ReglasNavegacionDashboard.estaSeleccionada(
            "Perfil", "Perfil"
        ) shouldBe true
    }

    // PRUEBA 6
    @Test
    fun catalogoMantieneInicioSeleccionado() {
        ReglasNavegacionDashboard.estaSeleccionada(
            "Inicio", "Catalogo"
        ) shouldBe true
    }

    // PRUEBA 7
    @Test
    fun catalogoNoSeleccionaMapa() {
        ReglasNavegacionDashboard.estaSeleccionada(
            "Mapa", "Catalogo"
        ) shouldBe false
    }

    // PRUEBA 8
    @Test
    fun detalleNoSeleccionaInicio() {
        ReglasNavegacionDashboard.estaSeleccionada(
            "Inicio", "Detalle"
        ) shouldBe false
    }

    // PRUEBA 9
    @Test
    fun clienteVeCarritoDesdeInicio() {
        ReglasNavegacionDashboard.mostrarAccesoCarrito(
            "Cliente", "Inicio"
        ) shouldBe true
    }

    // PRUEBA 10
    @Test
    fun clienteVeCarritoDesdeCatalogo() {
        ReglasNavegacionDashboard.mostrarAccesoCarrito(
            "Cliente", "Catalogo"
        ) shouldBe true
    }

    // PRUEBA 11
    @Test
    fun ocultarAccesoDuranteCompra() {

        listOf(
            "Carrito",
            "Confirmacion",
            "PedidoExito"
        ).forEach { pantalla ->

            ReglasNavegacionDashboard.mostrarAccesoCarrito(
                "Cliente", pantalla
            ) shouldBe false
        }
    }

    // PRUEBA 12
    @Test
    fun administradorNoVeAccesoCarrito() {
        ReglasNavegacionDashboard.mostrarAccesoCarrito(
            "Administrador", "Inicio"
        ) shouldBe false
    }

    // PRUEBA 13
    @Test
    fun rolDesconocidoNoVeAccesoCarrito() {
        ReglasNavegacionDashboard.mostrarAccesoCarrito(
            "Invitado", "Inicio"
        ) shouldBe false
    }


    // PRUEBA 14
    @Test
    fun carritoVacioMuestraCero() {
        ReglasNavegacionDashboard
            .etiquetaCantidadCarrito(0) shouldBe "0"
    }

    // PRUEBA 15
    @Test
    fun carritoConUnaUnidadMuestraUno() {
        ReglasNavegacionDashboard
            .etiquetaCantidadCarrito(1) shouldBe "1"
    }

    // PRUEBA 16
    @Test
    fun carritoCon99UnidadesMuestra99() {
        ReglasNavegacionDashboard
            .etiquetaCantidadCarrito(99) shouldBe "99"
    }

    // PRUEBA 17
    @Test
    fun carritoCon100UnidadesMuestra99Mas() {
        ReglasNavegacionDashboard
            .etiquetaCantidadCarrito(100) shouldBe "99+"
    }

    // PRUEBA 18
    @Test
    fun carritoConMuchasUnidadesMantiene99Mas() {
        ReglasNavegacionDashboard
            .etiquetaCantidadCarrito(500) shouldBe "99+"
    }

}

