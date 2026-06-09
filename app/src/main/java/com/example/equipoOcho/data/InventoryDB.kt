package com.example.equipoOcho.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.equipoOcho.model.Inventory
import com.example.equipoOcho.utils.Constants.NAME_BD

@Database(
    entities = [Inventory::class],
    version = 3,
    exportSchema = false
)
abstract class InventoryDB : RoomDatabase() {

    abstract fun inventoryDao(): InventoryDao

    companion object {

        @Volatile
        private var INSTANCE: InventoryDB? = null

        fun getDatabase(context: Context): InventoryDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    InventoryDB::class.java,
                    NAME_BD
                )
                    // Para desarrollo, si cambias el schema y no quieres migraciones:
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
