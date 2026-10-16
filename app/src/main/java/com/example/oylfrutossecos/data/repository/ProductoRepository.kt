
package com.example.oylfrutossecos.data.repository

import com.example.oylfrutossecos.data.local.dao.ProductoDao
import com.example.oylfrutossecos.data.local.entity.ProductoEntity

import kotlinx.coroutines.flow.Flow

class ProductoRepository(
    private val productoDao: ProductoDao
) {

    // Obtener todos los productos
    val productos: Flow<List<ProductoEntity>> =
        productoDao.obtenerProductos()

    // Obtener producto por ID
    suspend fun obtenerProductoPorId(
        id: Int
    ): ProductoEntity? {
        return productoDao.obtenerProductoPorId(id)
    }

    // Guardar producto
    suspend fun guardarProducto(
        producto: ProductoEntity
    ) {
        productoDao.guardarProducto(producto)
    }

    // Actualizar producto
    suspend fun actualizarProducto(
        producto: ProductoEntity
    ) {
        productoDao.actualizarProducto(producto)
    }

    // Eliminar producto
    suspend fun eliminarProducto(
        producto: ProductoEntity
    ) {
        productoDao.eliminarProducto(producto)
    }

    // Crear un producto con fotografía opcional
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

            // NUEVO: Guardar dirección de la foto
            imagenUri = imagenUri,

            activo = true
        )

        productoDao.insertarProducto(nuevoProducto)
    }

    // Editar un producto existente
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

            // NUEVO: Actualizar fotografía
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
}
