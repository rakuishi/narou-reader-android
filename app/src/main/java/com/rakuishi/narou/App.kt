package com.rakuishi.narou

import android.app.Application
import com.rakuishi.narou.database.AppDatabase

class App : Application() {

    lateinit var appDatabase: AppDatabase

    override fun onCreate() {
        super.onCreate()
        appDatabase = AppDatabase.getDatabase(this)
    }
}