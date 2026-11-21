package com.example.oylfrutossecos.data.repository

import com.example.oylfrutossecos.data.model.ProductoCatalogo
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class CatalogoRemoto(host: String) {
    private val productos = cliente(host, 8081).create(ProductosApi::class.java)
    private val inventario = cliente(host, 8082).create(InventarioApi::class.java)

    suspend fun cargar(): List<ProductoCatalogo> = coroutineScope {
        val existencias = async {
            inventario.listar().associate { it.productoId to it.stockDisponible }
        }
        val catalogo = productos.listar()
        val stock = existencias.await()
        catalogo.map { producto ->
            ProductoCatalogo(
                id = producto.id,
                nombre = producto.nombre,
                categoria = producto.categoria,
                precio = producto.precio,
                stock = stock[producto.id] ?: 0,
                descripcion = producto.descripcion.orEmpty(),
                presentacion = producto.presentacion.orEmpty(),
                imagenUri = producto.imagenUri,
                activo = producto.activo
            )
        }
    }

    private fun cliente(host: String, puerto: Int): Retrofit = Retrofit.Builder()
        .baseUrl("http://$host:$puerto/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private interface ProductosApi {
        @GET("api/productos")
        suspend fun listar(): List<ProductoRespuesta>
    }

    private interface InventarioApi {
        @GET("api/inventario")
        suspend fun listar(): List<InventarioRespuesta>
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
}
