
package com.example.oylfrutossecos.feature.pedidos.domain

import io.kotest.matchers.shouldBe
import org.junit.Test

class ReglasEstadoPedidoTest {

    @Test
    fun pendientePermitePreparacionYCancelacion() {
        ReglasEstadoPedido.estadosSiguientes(
            "Pendiente", "Retiro"
        ) shouldBe listOf(
            "En preparación", "Cancelado"
        )
    }

    @Test
    fun preparacionConRetiro() {
        ReglasEstadoPedido.estadosSiguientes(
            "En preparación", "Retiro"
        ) shouldBe listOf(
            "Listo para retiro", "Cancelado"
        )
    }

    @Test
    fun preparacionConDespacho() {
        ReglasEstadoPedido.estadosSiguientes(
            "En preparación", "Despacho"
        ) shouldBe listOf(
            "En despacho", "Cancelado"
        )
    }

    @Test
    fun listoParaRetiroPermiteEntrega() {
        ReglasEstadoPedido.estadosSiguientes(
            "Listo para retiro", "Retiro"
        ) shouldBe listOf("Entregado")
    }

    @Test
    fun enDespachoPermiteEntrega() {
        ReglasEstadoPedido.estadosSiguientes(
            "En despacho", "Despacho"
        ) shouldBe listOf("Entregado")
    }

    @Test
    fun entregadoNoPermiteCambios() {
        ReglasEstadoPedido.estadosSiguientes(
            "Entregado", "Retiro"
        ) shouldBe emptyList()
    }

    @Test
    fun canceladoNoPermiteCambios() {
        ReglasEstadoPedido.estadosSiguientes(
            "Cancelado", "Retiro"
        ) shouldBe emptyList()
    }

    @Test
    fun permitirTransicionCorrecta() {
        ReglasEstadoPedido.puedeCambiarEstado(
            "Pendiente",
            "En preparación",
            "Despacho"
        ) shouldBe true
    }

    @Test
    fun rechazarTransicionDirectaAEntregado() {
        ReglasEstadoPedido.puedeCambiarEstado(
            "Pendiente",
            "Entregado",
            "Retiro"
        ) shouldBe false
    }

    @Test
    fun rechazarEntregaDePedidoCancelado() {
        ReglasEstadoPedido.puedeCambiarEstado(
            "Cancelado",
            "Entregado",
            "Despacho"
        ) shouldBe false
    }

    @Test
    fun rechazarPreparacionRepetida() {
        ReglasEstadoPedido.puedeCambiarEstado(
            "En preparación",
            "En preparación",
            "Retiro"
        ) shouldBe false
    }

    @Test
    fun rechazarEstadoDesconocido() {
        ReglasEstadoPedido.puedeCambiarEstado(
            "Estado desconocido",
            "Entregado",
            "Retiro"
        ) shouldBe false
    }
}
