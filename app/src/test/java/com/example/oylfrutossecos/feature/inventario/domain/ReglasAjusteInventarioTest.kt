
package com.example.oylfrutossecos.feature.inventario.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.Test

class ReglasAjusteInventarioTest {

    // PRUEBA 1: Cantidad positiva válida
    @Test
    fun cantidadPositivaValida() {
        ReglasAjusteInventario.validarCantidad(
            "5"
        ) shouldBe null
    }

    // PRUEBA 2: Permitir espacios alrededor
    @Test
    fun cantidadConEspacios() {
        ReglasAjusteInventario.validarCantidad(
            " 12 "
        ) shouldBe null
    }

    // PRUEBA 3: Campo vacío
    @Test
    fun cantidadVacia() {
        ReglasAjusteInventario.validarCantidad(
            ""
        ) shouldBe "Ingresa una cantidad entera válida"
    }

    // PRUEBA 4: Texto en vez de número
    @Test
    fun cantidadConLetras() {
        ReglasAjusteInventario.validarCantidad(
            "cinco"
        ) shouldBe "Ingresa una cantidad entera válida"
    }

    // PRUEBA 5: Cantidad decimal
    @Test
    fun cantidadDecimal() {
        ReglasAjusteInventario.validarCantidad(
            "2.5"
        ) shouldBe "Ingresa una cantidad entera válida"
    }

    // PRUEBA 6: Cantidad cero
    @Test
    fun cantidadCero() {
        ReglasAjusteInventario.validarCantidad(
            "0"
        ) shouldBe "La cantidad debe ser mayor que cero"
    }

    // PRUEBA 7: Cantidad negativa
    @Test
    fun cantidadNegativa() {
        ReglasAjusteInventario.validarCantidad(
            "-5"
        ) shouldBe "La cantidad debe ser mayor que cero"
    }

    // PRUEBA 8: Número fuera del rango de Int
    @Test
    fun cantidadDemasiadoGrande() {
        ReglasAjusteInventario.validarCantidad(
            "2147483648"
        ) shouldBe "Ingresa una cantidad entera válida"
    }

    // PRUEBA 9: Máximo número entero permitido
    @Test
    fun cantidadMaximaValida() {
        ReglasAjusteInventario.validarCantidad(
            Int.MAX_VALUE.toString()
        ) shouldBe null
    }

    // PRUEBA 10: Entrada de inventario
    @Test
    fun entradaGeneraVariacionPositiva() {
        ReglasAjusteInventario.calcularVariacion(
            cantidad = 10,
            esEntrada = true
        ) shouldBe 10
    }

    // PRUEBA 11: Salida de inventario
    @Test
    fun salidaGeneraVariacionNegativa() {
        ReglasAjusteInventario.calcularVariacion(
            cantidad = 10,
            esEntrada = false
        ) shouldBe -10
    }

    // PRUEBA 12: No calcular variación de cero
    @Test
    fun variacionConCeroLanzaError() {
        shouldThrow<IllegalArgumentException> {
            ReglasAjusteInventario.calcularVariacion(
                cantidad = 0,
                esEntrada = true
            )
        }
    }
}
