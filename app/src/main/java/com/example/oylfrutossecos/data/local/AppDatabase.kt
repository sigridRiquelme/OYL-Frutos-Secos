
package com.example.oylfrutossecos.data.local

import android.content.Context

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

import com.example.oylfrutossecos.data.local.dao.ProductoDao
import com.example.oylfrutossecos.data.local.entity.ProductoEntity

@Database(
    entities = [ProductoEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productoDao(): ProductoDao

    companion object {

        // Migración de versión 1 a versión 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {

            override fun migrate(db: SupportSQLiteDatabase) {

                // Agregar columna sin borrar los productos
                db.execSQL(
                    "ALTER TABLE productos " +
                            "ADD COLUMN activo INTEGER NOT NULL DEFAULT 1"
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "oyl_frutos_secos.db"
                )
                    // Registrar la migración
                    .addMigrations(MIGRATION_1_2)
                    .build()

                INSTANCE = instancia

                instancia
            }
        }
    }
}
