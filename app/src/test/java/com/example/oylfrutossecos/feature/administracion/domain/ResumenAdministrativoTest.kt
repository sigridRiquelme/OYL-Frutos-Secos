
package com.example.oylfrutossecos.feature.administracion.domain

import com.example.oylfrutossecos.data.model.Pedido
import com.example.oylfrutossecos.data.model.ProductoCatalogo

import io.kotest.matchers.shouldBe
import org.junit.Test

class ResumenAdministrativoTest {

    private fun producto(
        id: Int,
        stock: Int,
        activo: Boolean = true
    ) = ProductoCatalogo(
        id = id,
        nombre = "Producto $id",
        categoria = "Frutos secos",
        precio = 3000,
        stock = stock,
        activo = activo
    )

    private fun pedido(
        numero: String,
        estado: String
    ) = Pedido(
        numero = numero,
        fecha = 123456789L,
        clienteId = 1,
        nombreCliente = "Cliente prueba",
        telefono = "912345678",
        modalidad = "Retiro",
        direccion = "",
        productos = emptyList(),
        total = 3000,
        estado = estado
    )

    // PRUEBA 1
    @Test
    fun sinProductosNiPedidosDevuelveCeros() {

        val resumen = CalculadoraResumenAdministrativo.calcular(
            productos = emptyList(),
            pedidos = emptyList()
        )

        resumen.productosActivos shouldBe 0
        resumen.alertasStock shouldBe 0
        resumen.pedidosPendientes shouldBe 0
        resumen.pedidosRegistrados shouldBe 0
        resumen.totalAvisos shouldBe 0
    }

    // PRUEBA 2
    @Test
    fun cuentaSoloProductosActivos() {

        val resumen = CalculadoraResumenAdministrativo.calcular(
            productos = listOf(
                producto(1, 20),
                producto(2, 15),
                producto(3, 10, activo = false)
            ),
            pedidos = emptyList()
        )

        resumen.productosActivos shouldBe 2
    }

    // PRUEBA 3
    @Test
    fun productoSinStockCuentaComoAgotado() {

        val resumen = CalculadoraResumenAdministrativo.calcular(
            productos = listOf(producto(1, 0)),
            pedidos = emptyList()
        )

        resumen.productosAgotados shouldBe 1
        resumen.productosStockBajo shouldBe 0
        resumen.alertasStock shouldBe 1
    }

    // PRUEBA 4
    @Test
    fun stockEntreUnoYDiezGeneraAlerta() {

        val resumen = CalculadoraResumenAdministrativo.calcular(
            productos = listOf(
                producto(1, 1),
                producto(2, 5),
                producto(3, 10)
            ),
            pedidos = emptyList()
        )

        resumen.productosStockBajo shouldBe 3
        resumen.alertasStock shouldBe 3
    }

    // PRUEBA 5
    @Test
    fun stockSuperiorADiezNoGeneraAlerta() {

        val resumen = CalculadoraResumenAdministrativo.calcular(
            productos = listOf(
                producto(1, 11),
                producto(2, 30)
            ),
            pedidos = emptyList()
        )

        resumen.productosActivos shouldBe 2
        resumen.alertasStock shouldBe 0
    }

    // PRUEBA 6
    @Test
    fun productosInactivosNoGeneranAlertas() {

        val resumen = CalculadoraResumenAdministrativo.calcular(
            productos = listOf(
                producto(1, 0, activo = false),
                producto(2, 5, activo = false)
            ),
            pedidos = emptyList()
        )

        resumen.productosAgotados shouldBe 0
        resumen.productosStockBajo shouldBe 0
        resumen.alertasStock shouldBe 0
    }

    // PRUEBA 7
    @Test
    fun cuentaSoloPedidosPendientes() {

        val resumen = CalculadoraResumenAdministrativo.calcular(
            productos = emptyList(),
            pedidos = listOf(
                pedido("P001", "Pendiente"),
                pedido("P002", "En preparación"),
                pedido("P003", "Pendiente"),
                pedido("P004", "Entregado")
            )
        )

        resumen.pedidosPendientes shouldBe 2
        resumen.pedidosRegistrados shouldBe 4
    }

    // PRUEBA 8
    @Test
    fun sumaCorrectamenteTodosLosAvisos() {

        val resumen = CalculadoraResumenAdministrativo.calcular(
            productos = listOf(
                producto(1, 0),
                producto(2, 5),
                producto(3, 20)
            ),
            pedidos = listOf(
                pedido("P001", "Pendiente"),
                pedido("P002", "Pendiente"),
                pedido("P003", "En preparación")
            )
        )

        resumen.productosActivos shouldBe 3
        resumen.productosAgotados shouldBe 1
        resumen.productosStockBajo shouldBe 1
        resumen.alertasStock shouldBe 2
        resumen.pedidosPendientes shouldBe 2
        resumen.pedidosRegistrados shouldBe 3
        resumen.totalAvisos shouldBe 4
    }
}
