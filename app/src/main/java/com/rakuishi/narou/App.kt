package com.rakuishi.narou

import android.app.Application
import com.rakuishi.narou.data.NovelRepository
import com.rakuishi.narou.database.AppDatabase

class App : Application() {

    lateinit var appDatabase: AppDatabase
    lateinit var novelRepository: NovelRepository

    override fun onCreate() {
        super.onCreate()
        appDatabase = AppDatabase.getDatabase(this)
        novelRepository = NovelRepository(appDatabase.novelDao())
    }
}