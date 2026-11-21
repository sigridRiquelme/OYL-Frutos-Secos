package com.example.oylfrutossecos

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import com.example.oylfrutossecos.ui.components.PantallasExploracionCliente
import org.junit.Rule
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

@LargeTest
@RunWith(AndroidJUnit4::class)
class CatalogoBackendTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun pantallaMuestraProductosYStockDelBackend() {
        val host = InstrumentationRegistry.getArguments().getString("oylBackendHost")
        assumeTrue("Configura oylBackendHost", host == BuildConfig.CATALOGO_HOST && !host.isNullOrBlank())
        compose.setContent {
            PantallasExploracionCliente(
                pantalla = "Catalogo",
                productos = emptyList(),
                productoSeleccionado = null,
                rol = "Cliente",
                cantidadesCarrito = emptyMap(),
                onVerDetalle = {},
                onVolverCatalogo = {},
                onAgregar = { _, _ -> }
            )
        }
        compose.waitUntil(15_000) {
            compose.onAllNodesWithText("Almendras naturales").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Almendras naturales").assertIsDisplayed()
        compose.onNodeWithText("En stock · 25 un.").assertIsDisplayed()
    }

    @Test
    fun consultaCatalogoPublicoDesdeAndroid() {
        val host = InstrumentationRegistry.getArguments().getString("oylBackendHost")
        assumeTrue(
            "Configura oylBackendHost con la dirección local del backend",
            !host.isNullOrBlank()
        )
        val api = Retrofit.Builder()
            .baseUrl("http://$host:8081/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProductosApi::class.java)

        val respuesta = api.listar().execute()
        assertEquals("Respuesta de GET /api/productos", 200, respuesta.code())
        assertNotNull("El backend debe devolver una lista JSON", respuesta.body())
    }

    interface ProductosApi {
        @GET("api/productos")
        fun listar(): Call<List<ProductoRemoto>>
    }

    data class ProductoRemoto(val id: Long, val nombre: String)
}
