package com.rakuishi.nreader.database

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rakuishi.nreader.BuildConfig
import com.rakuishi.nreader.model.Novel
import com.rakuishi.nreader.util.TypeConverter
import com.rakuishi.nreader.util.SampleDataProvider

@Database(
    entities = [
        Novel::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(
    TypeConverter::class,
)
abstract class AppDatabase : RoomDatabase() {

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(appContext: Application): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(appContext, AppDatabase::class.java, "app.db")
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            if (BuildConfig.DEBUG) {
                                db.execSQL(SampleDataProvider.sql())
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    abstract fun novelDao(): NovelDao
}