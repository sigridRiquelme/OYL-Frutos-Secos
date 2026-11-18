
package com.example.oylfrutossecos.feature.pedidos.domain

import io.kotest.matchers.shouldBe
import org.junit.Test

class ValidadorPedidoTest {

    // PRUEBA 1: Nombre válido
    @Test
    fun nombreCorrecto() {
        ValidadorPedido.esNombreValido(
            "Sebastian"
        ) shouldBe true
    }

    // PRUEBA 2: Nombre demasiado corto
    @Test
    fun nombreDemasiadoCorto() {
        ValidadorPedido.esNombreValido(
            "Ab"
        ) shouldBe false
    }

    // PRUEBA 3: Teléfono válido
    @Test
    fun telefonoCorrecto() {
        ValidadorPedido.esTelefonoValido(
            "912345678"
        ) shouldBe true
    }

    // PRUEBA 4: Teléfono inválido
    @Test
    fun telefonoInvalido() {
        ValidadorPedido.esTelefonoValido(
            "12345"
        ) shouldBe false
    }

    // PRUEBA 5: Retiro sin dirección
    @Test
    fun retiroNoRequiereDireccion() {
        ValidadorPedido.esDireccionValida(
            modalidad = "Retiro",
            direccion = ""
        ) shouldBe true
    }

    // PRUEBA 6: Despacho con dirección inválida
    @Test
    fun despachoRequiereDireccion() {
        ValidadorPedido.esDireccionValida(
            modalidad = "Despacho",
            direccion = "Calle 1"
        ) shouldBe false
    }

    // PRUEBA 7: Pedido válido
    @Test
    fun pedidoCorrecto() {
        ValidadorPedido.puedeConfirmar(
            nombre = "Cliente Prueba",
            telefono = "912345678",
            modalidad = "Retiro",
            direccion = "",
            tieneProductos = true
        ) shouldBe true
    }

    // PRUEBA 8: Pedido sin productos
    @Test
    fun pedidoSinProductos() {
        ValidadorPedido.puedeConfirmar(
            nombre = "Cliente Prueba",
            telefono = "912345678",
            modalidad = "Retiro",
            direccion = "",
            tieneProductos = false
        ) shouldBe false
    }
}
