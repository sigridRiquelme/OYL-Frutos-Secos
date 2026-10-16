
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

    // 1. Obtener todos los productos
    @Query("SELECT * FROM productos ORDER BY nombre ASC")
    fun obtenerProductos(): Flow<List<ProductoEntity>>

    // 2. Buscar un producto por su ID
    @Query("SELECT * FROM productos WHERE id = :id LIMIT 1")
    suspend fun obtenerProductoPorId(id: Int): ProductoEntity?

    // 3. Guardar un producto
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarProducto(producto: ProductoEntity)

    // 4. Guardar una lista de productos
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarProductos(productos: List<ProductoEntity>)

    // 5. Actualizar un producto
    @Update
    suspend fun actualizarProducto(producto: ProductoEntity)

    // 6. Eliminar un producto
    @Delete
    suspend fun eliminarProducto(producto: ProductoEntity)

    @Query("SELECT COUNT(*) FROM productos")
    suspend fun contarProductos(): Int


    // Obtener un ID disponible para un nuevo producto
    @Query("SELECT COALESCE(MAX(id), 0) + 1 FROM productos")
    suspend fun obtenerSiguienteId(): Int

    // Insertar un producto nuevo sin reemplazar otro
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarProducto(producto: ProductoEntity)

}
