
package com.example.oylfrutossecos.feature.navegacion.domain

import io.kotest.matchers.shouldBe
import org.junit.Test

class NavegacionClienteTest {

    private fun navegar(
        actual: String,
        accion: AccionCliente
    ): String {
        return NavegacionCliente.siguientePantalla(
            actual,
            accion
        )
    }

    // PRUEBA 1
    @Test
    fun abreCatalogoDesdeInicio() {

        navegar(
            "Inicio",
            AccionCliente.ABRIR_CATALOGO
        ) shouldBe "Catalogo"
    }

    // PRUEBA 2
    @Test
    fun abreDetalleDesdeCatalogo() {

        navegar(
            "Catalogo",
            AccionCliente.VER_DETALLE
        ) shouldBe "Detalle"
    }

    // PRUEBA 3
    @Test
    fun abreDetalleDesdeQR() {

        navegar(
            "QR",
            AccionCliente.VER_DETALLE
        ) shouldBe "Detalle"
    }

    // PRUEBA 4
    @Test
    fun noAbreDetalleDesdeInicio() {

        navegar(
            "Inicio",
            AccionCliente.VER_DETALLE
        ) shouldBe "Inicio"
    }

    // PRUEBA 5
    @Test
    fun vuelveAlCatalogoDesdeDetalle() {

        navegar(
            "Detalle",
            AccionCliente.VOLVER_A_CATALOGO
        ) shouldBe "Catalogo"
    }

    // PRUEBA 6
    @Test
    fun abreCarritoDesdeInicio() {

        navegar(
            "Inicio",
            AccionCliente.ABRIR_CARRITO
        ) shouldBe "Carrito"
    }

    // PRUEBA 7
    @Test
    fun carritoValidoAvanzaAConfirmacion() {

        navegar(
            "Carrito",
            AccionCliente.CONTINUAR_A_CONFIRMACION
        ) shouldBe "Confirmacion"
    }

    // PRUEBA 8
    @Test
    fun noConfirmaDesdeOtraPantalla() {

        navegar(
            "Inicio",
            AccionCliente.CONTINUAR_A_CONFIRMACION
        ) shouldBe "Inicio"
    }

    // PRUEBA 9
    @Test
    fun vuelveAlCarritoDesdeConfirmacion() {

        navegar(
            "Confirmacion",
            AccionCliente.VOLVER_A_CARRITO
        ) shouldBe "Carrito"
    }

    // PRUEBA 10
    @Test
    fun pedidoRegistradoAbrePantallaDeExito() {

        navegar(
            "Confirmacion",
            AccionCliente.PEDIDO_REGISTRADO
        ) shouldBe "PedidoExito"
    }

    // PRUEBA 11
    @Test
    fun noAbreExitoDesdeOtraPantalla() {

        navegar(
            "Carrito",
            AccionCliente.PEDIDO_REGISTRADO
        ) shouldBe "Carrito"
    }

    // PRUEBA 12
    @Test
    fun pedidoExitosoPermiteVolverAlInicio() {

        navegar(
            "PedidoExito",
            AccionCliente.VOLVER_A_INICIO
        ) shouldBe "Inicio"
    }
}
