
package com.example.oylfrutossecos.feature.productos.domain

import io.kotest.matchers.shouldBe
import org.junit.Test

class ValidadorProductoTest {

    // Producto de prueba válido
    private val datosValidos = DatosNuevoProducto(
        nombre = "Almendras",
        categoria = "Frutos secos",
        precio = "3500",
        stock = "20",
        descripcion = "Almendras naturales",
        presentacion = "Bolsa de 250 g"
    )

    // PRUEBA 1: Producto válido
    @Test
    fun productoCorrecto() {
        val errores = ValidadorProducto.validar(
            datosValidos
        )

        errores.isEmpty() shouldBe true
    }

    // PRUEBA 2: Nombre corto
    @Test
    fun nombreDemasiadoCorto() {
        val errores = ValidadorProducto.validar(
            datosValidos.copy(nombre = "Ab")
        )

        errores["nombre"] shouldBe
                "El nombre debe tener al menos 3 caracteres"
    }

    // PRUEBA 3: Nombre con espacios
    @Test
    fun nombreConEspaciosValido() {
        val errores = ValidadorProducto.validar(
            datosValidos.copy(nombre = "  Ana  ")
        )

        errores.isEmpty() shouldBe true
    }

    // PRUEBA 4: Categoría vacía
    @Test
    fun categoriaVacia() {
        val errores = ValidadorProducto.validar(
            datosValidos.copy(categoria = "")
        )

        errores["categoria"] shouldBe
                "Selecciona una categoría"
    }

    // PRUEBA 5: Precio cero
    @Test
    fun precioCero() {
        val errores = ValidadorProducto.validar(
            datosValidos.copy(precio = "0")
        )

        errores["precio"] shouldBe
                "Ingresa un precio mayor a cero"
    }

    // PRUEBA 6: Precio negativo
    @Test
    fun precioNegativo() {
        val errores = ValidadorProducto.validar(
            datosValidos.copy(precio = "-500")
        )

        errores.containsKey("precio") shouldBe true
    }

    // PRUEBA 7: Precio con letras
    @Test
    fun precioConLetras() {
        val errores = ValidadorProducto.validar(
            datosValidos.copy(precio = "tres mil")
        )

        errores.containsKey("precio") shouldBe true
    }

    // PRUEBA 8: Precio fuera del rango permitido
    @Test
    fun precioDemasiadoGrande() {
        val errores = ValidadorProducto.validar(
            datosValidos.copy(precio = "2147483648")
        )

        errores.containsKey("precio") shouldBe true
    }

    // PRUEBA 9: Stock cero permitido
    @Test
    fun stockCeroEsValido() {
        val errores = ValidadorProducto.validar(
            datosValidos.copy(stock = "0")
        )

        errores.isEmpty() shouldBe true
    }

    // PRUEBA 10: Stock negativo
    @Test
    fun stockNegativo() {
        val errores = ValidadorProducto.validar(
            datosValidos.copy(stock = "-5")
        )

        errores["stock"] shouldBe
                "Ingresa un stock válido (0 o más)"
    }

    // PRUEBA 11: Stock con letras
    @Test
    fun stockConLetras() {
        val errores = ValidadorProducto.validar(
            datosValidos.copy(stock = "diez")
        )

        errores.containsKey("stock") shouldBe true
    }

    // PRUEBA 12: Descripción vacía
    @Test
    fun descripcionVacia() {
        val errores = ValidadorProducto.validar(
            datosValidos.copy(descripcion = "")
        )

        errores["descripcion"] shouldBe
                "Ingresa una descripción"
    }

    // PRUEBA 13: Presentación vacía
    @Test
    fun presentacionVacia() {
        val errores = ValidadorProducto.validar(
            datosValidos.copy(presentacion = "")
        )

        errores["presentacion"] shouldBe
                "Ingresa una presentación"
    }

    // PRUEBA 14: Varios errores simultáneos
    @Test
    fun detectarMultiplesErrores() {
        val errores = ValidadorProducto.validar(
            datosValidos.copy(
                nombre = "A",
                categoria = "",
                precio = "0",
                stock = "-1",
                descripcion = "",
                presentacion = ""
            )
        )

        errores.keys shouldBe setOf(
            "nombre",
            "categoria",
            "precio",
            "stock",
            "descripcion",
            "presentacion"
        )
    }
}
