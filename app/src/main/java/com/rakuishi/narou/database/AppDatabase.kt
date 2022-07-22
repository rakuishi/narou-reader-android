package com.rakuishi.narou.database

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
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
                val instance = Room.databaseBuilder(appContext, AppDatabase::class.java, "app.db")
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)

                            // sample data
                            val current = System.currentTimeMillis()
                            val sql = "INSERT INTO novels VALUES " +
                                    "(NULL, 'n4811fg', 'TRPGプレイヤーが異世界で最強ビルドを目指す～ヘンダーソン氏の福音を～', 'Schuld', 241, $current, 1, 0)," +
                                    "(NULL, 'n3556o', 'Knight''s & Magic', '天酒之瓢', 204, $current, 1, 0)," +
                                    "(NULL, 'n5881cl', '賢者の孫', '吉岡剛', 299, $current, 1, 0)"
                            db.execSQL(sql)
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