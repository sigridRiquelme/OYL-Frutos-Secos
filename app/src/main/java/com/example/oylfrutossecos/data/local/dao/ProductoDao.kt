
package com.example.oylfrutossecos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import androidx.room.OnConflictStrategy

import com.example.oylfrutossecos.data.local.entity.ProductoEntity

import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {

    // Obtener todos los productos
    @Query("SELECT * FROM productos ORDER BY nombre ASC")
    fun obtenerProductos(): Flow<List<ProductoEntity>>

    // Buscar producto por ID
    @Query("SELECT * FROM productos WHERE id = :id LIMIT 1")
    suspend fun obtenerProductoPorId(id: Int): ProductoEntity?

    // Guardar producto
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarProducto(producto: ProductoEntity)

    // Guardar lista de productos
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarProductos(productos: List<ProductoEntity>)

    // Actualizar producto
    @Update
    suspend fun actualizarProducto(producto: ProductoEntity)

    // Eliminar producto
    @Delete
    suspend fun eliminarProducto(producto: ProductoEntity)

    // Contar productos
    @Query("SELECT COUNT(*) FROM productos")
    suspend fun contarProductos(): Int

    // Obtener siguiente ID
    @Query("SELECT COALESCE(MAX(id), 0) + 1 FROM productos")
    suspend fun obtenerSiguienteId(): Int

    // Insertar nuevo producto
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarProducto(producto: ProductoEntity)

    // NUEVO: Activar o desactivar un producto
    @Query(
        "UPDATE productos SET activo = :activo WHERE id = :id"
    )
    suspend fun cambiarEstadoProducto(
        id: Int,
        activo: Boolean
    ): Int
}
