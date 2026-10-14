
package com.example.oylfrutossecos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.oylfrutossecos.data.local.dao.ProductoDao
import com.example.oylfrutossecos.data.local.entity.ProductoEntity
import com.example.oylfrutossecos.data.local.entity.ClienteEntity
@Database(
    entities = [
        ProductoEntity::class,
        ClienteEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productoDao(): ProductoDao
    abstract fun clienteDao():
            com.example.oylfrutossecos.data.local.dao.ClienteDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "oyl_frutos_secos.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()

                INSTANCE = instancia

                instancia
            }
        }
    }
}
