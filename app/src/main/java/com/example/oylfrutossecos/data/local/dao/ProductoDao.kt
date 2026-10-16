
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

    // Obtener producto por ID
    @Query("SELECT * FROM productos WHERE id = :id LIMIT 1")
    suspend fun obtenerProductoPorId(id: Int): ProductoEntity?

    // Guardar producto
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarProducto(producto: ProductoEntity)

    // Guardar lista
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

    // Insertar producto nuevo
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarProducto(producto: ProductoEntity)

    // Activar o desactivar producto
    @Query(
        "UPDATE productos SET activo = :activo WHERE id = :id"
    )
    suspend fun cambiarEstadoProducto(
        id: Int,
        activo: Boolean
    ): Int

    // NUEVO: Ajustar existencias de manera segura
    @Query(
        """
        UPDATE productos
        SET stock = stock + :variacion
        WHERE id = :id
        AND (stock + :variacion) BETWEEN 0 AND 2147483647
        """
    )
    suspend fun ajustarStock(
        id: Int,
        variacion: Int
    ): Int
}
