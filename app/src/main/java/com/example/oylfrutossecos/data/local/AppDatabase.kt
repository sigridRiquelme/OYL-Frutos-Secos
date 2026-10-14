
package com.example.oylfrutossecos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.oylfrutossecos.data.local.dao.ProductoDao
import com.example.oylfrutossecos.data.local.entity.ProductoEntity

@Database(
    entities = [ProductoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productoDao(): ProductoDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "oyl_frutos_secos.db"
                ).build()

                INSTANCE = instancia

                instancia
            }
        }
    }
}
