package com.example.oylfrutossecos.data.repository

import com.example.oylfrutossecos.data.model.ProductoCatalogo
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import com.example.oylfrutossecos.feature.productos.domain.DatosNuevoProducto

class CatalogoRemoto(host: String) {
    private val http = OkHttpClient.Builder().retryOnConnectionFailure(false).build()
    private val productos = cliente(host, 8081).create(ProductosApi::class.java)
    private val inventario = cliente(host, 8082).create(InventarioApi::class.java)

    suspend fun cargar(): List<ProductoCatalogo> = coroutineScope {
        val existencias = async {
            inventario.listar().associate { it.productoId to it.stockDisponible }
        }
        val catalogo = productos.listar()
        val stock = existencias.await()
        catalogo.map { producto -> producto.alCatalogo(stock[producto.id] ?: 0) }
    }

    suspend fun editar(sesion: SesionRemota, id: Int, datos: DatosNuevoProducto, stock: Int): ProductoCatalogo =
        productos.editar(
            "Bearer ${sesion.token}", id, EditarProducto(
                datos.nombre, datos.categoria, datos.precio.toInt(), datos.descripcion,
                datos.presentacion, datos.imagenUri
            )
        ).alCatalogo(stock)

    suspend fun cambiarEstado(sesion: SesionRemota, id: Int, activo: Boolean, stock: Int): ProductoCatalogo =
        productos.cambiarEstado("Bearer ${sesion.token}", id, EstadoProducto(activo)).alCatalogo(stock)

    suspend fun ajustarStock(sesion: SesionRemota, id: Int, cantidad: Int, entrada: Boolean): Int =
        inventario.ajustar(
            "Bearer ${sesion.token}", id, AjusteStock(if (entrada) "ENTRADA" else "SALIDA", cantidad)
        ).stockDisponible

    private fun ProductoRespuesta.alCatalogo(stock: Int): ProductoCatalogo =
        ProductoCatalogo(
            id = id, nombre = nombre, categoria = categoria, precio = precio, stock = stock,
            descripcion = descripcion.orEmpty(), presentacion = presentacion.orEmpty(),
            imagenUri = imagenUri, activo = activo
        )

    private fun cliente(host: String, puerto: Int): Retrofit = Retrofit.Builder()
        .baseUrl("http://$host:$puerto/")
        .client(http)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private interface ProductosApi {
        @GET("api/productos")
        suspend fun listar(): List<ProductoRespuesta>

        @PUT("api/productos/{id}")
        suspend fun editar(@Header("Authorization") token: String, @Path("id") id: Int,
                           @Body datos: EditarProducto): ProductoRespuesta

        @PATCH("api/productos/{id}/estado")
        suspend fun cambiarEstado(@Header("Authorization") token: String, @Path("id") id: Int,
                                  @Body datos: EstadoProducto): ProductoRespuesta
    }

    private interface InventarioApi {
        @GET("api/inventario")
        suspend fun listar(): List<InventarioRespuesta>

        @POST("api/inventario/{id}/ajustes")
        suspend fun ajustar(@Header("Authorization") token: String, @Path("id") id: Int,
                            @Body datos: AjusteStock): InventarioRespuesta
    }

    private data class ProductoRespuesta(
        val id: Int,
        val nombre: String,
        val categoria: String,
        val precio: Int,
        val descripcion: String?,
        val presentacion: String?,
        val imagenUri: String?,
        val activo: Boolean
    )

    private data class InventarioRespuesta(val productoId: Int, val stockDisponible: Int)
    private data class EstadoProducto(val activo: Boolean)
    private data class AjusteStock(val tipo: String, val cantidad: Int)
    private data class EditarProducto(
        val nombre: String, val categoria: String, val precio: Int,
        val descripcion: String, val presentacion: String, val imagenUri: String?
    )
}
