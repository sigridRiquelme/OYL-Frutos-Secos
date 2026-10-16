
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

    // Obtener un producto por su ID
    suspend fun obtenerProductoPorId(id: Int): ProductoEntity? {
        return productoDao.obtenerProductoPorId(id)
    }

    // Guardar un producto
    suspend fun guardarProducto(producto: ProductoEntity) {
        productoDao.guardarProducto(producto)
    }

    // Actualizar un producto
    suspend fun actualizarProducto(producto: ProductoEntity) {
        productoDao.actualizarProducto(producto)
    }

    // Eliminar un producto
    suspend fun eliminarProducto(producto: ProductoEntity) {
        productoDao.eliminarProducto(producto)
    }


    // Crear un nuevo producto en la base de datos
    suspend fun crearProducto(
        nombre: String,
        categoria: String,
        precio: Int,
        stock: Int,
        descripcion: String,
        presentacion: String
    ) {
        val nuevoId = productoDao.obtenerSiguienteId()

        val nuevoProducto = ProductoEntity(
            id = nuevoId,
            nombre = nombre,
            categoria = categoria,
            precio = precio,
            stock = stock,
            descripcion = descripcion,
            presentacion = presentacion
        )

        productoDao.insertarProducto(nuevoProducto)
    }

}
