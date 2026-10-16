
package com.example.oylfrutossecos.data.repository

import com.example.oylfrutossecos.data.local.dao.ProductoDao
import com.example.oylfrutossecos.data.local.entity.ProductoEntity

import kotlinx.coroutines.flow.Flow

class ProductoRepository(
    private val productoDao: ProductoDao
) {

    val productos: Flow<List<ProductoEntity>> =
        productoDao.obtenerProductos()

    suspend fun obtenerProductoPorId(
        id: Int
    ): ProductoEntity? {
        return productoDao.obtenerProductoPorId(id)
    }

    suspend fun guardarProducto(
        producto: ProductoEntity
    ) {
        productoDao.guardarProducto(producto)
    }

    suspend fun actualizarProducto(
        producto: ProductoEntity
    ) {
        productoDao.actualizarProducto(producto)
    }

    suspend fun eliminarProducto(
        producto: ProductoEntity
    ) {
        productoDao.eliminarProducto(producto)
    }

    // Crear producto con fotografía
    suspend fun crearProducto(
        nombre: String,
        categoria: String,
        precio: Int,
        stock: Int,
        descripcion: String,
        presentacion: String,
        imagenUri: String?
    ) {

        val nuevoId = productoDao.obtenerSiguienteId()

        val nuevoProducto = ProductoEntity(
            id = nuevoId,
            nombre = nombre,
            categoria = categoria,
            precio = precio,
            stock = stock,
            descripcion = descripcion,
            presentacion = presentacion,
            imagenUri = imagenUri,
            activo = true
        )

        productoDao.insertarProducto(nuevoProducto)
    }

    // Editar producto existente
    suspend fun editarProducto(
        id: Int,
        nombre: String,
        categoria: String,
        precio: Int,
        stock: Int,
        descripcion: String,
        presentacion: String,
        imagenUri: String?
    ) {

        val productoActual =
            productoDao.obtenerProductoPorId(id)
                ?: throw IllegalArgumentException(
                    "Producto no encontrado"
                )

        val productoModificado = productoActual.copy(
            nombre = nombre,
            categoria = categoria,
            precio = precio,
            stock = stock,
            descripcion = descripcion,
            presentacion = presentacion,
            imagenUri = imagenUri
        )

        productoDao.actualizarProducto(
            productoModificado
        )
    }

    // Activar o desactivar producto
    suspend fun cambiarEstadoProducto(
        id: Int,
        activo: Boolean
    ) {

        val filasModificadas =
            productoDao.cambiarEstadoProducto(
                id,
                activo
            )

        if (filasModificadas == 0) {
            throw IllegalArgumentException(
                "No se encontró el producto"
            )
        }
    }

    // NUEVO: Registrar entrada o salida de existencias
    suspend fun ajustarStock(
        id: Int,
        variacion: Int
    ) {

        require(variacion != 0) {
            "La variación no puede ser cero"
        }

        val filasModificadas =
            productoDao.ajustarStock(
                id = id,
                variacion = variacion
            )

        if (filasModificadas == 0) {
            throw IllegalStateException(
                "No se pudo ajustar el stock. " +
                        "Verifica las existencias disponibles."
            )
        }
    }
}
