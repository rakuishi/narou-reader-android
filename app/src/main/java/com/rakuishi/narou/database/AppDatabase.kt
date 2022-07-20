package com.rakuishi.narou.database

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rakuishi.narou.model.Novel
import com.rakuishi.narou.util.DateConverter

@Database(
    entities = [
        Novel::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(
    DateConverter::class,
)
abstract class AppDatabase : RoomDatabase() {

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(appContext: Application): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    appContext,
                    AppDatabase::class.java,
                    "app.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }

    abstract fun novelDao(): NovelDao
}