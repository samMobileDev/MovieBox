package com.example.moviebox

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FilmEntity::class],
    version = 1
)
abstract class FilmDatabase : RoomDatabase() {
    abstract fun filmDao(): FilmDao

    companion object{
        @Volatile
        private var INSTANCE: FilmDatabase? = null
        fun getDatabase(context: Context): FilmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    FilmDatabase::class.java,
                    "film_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
