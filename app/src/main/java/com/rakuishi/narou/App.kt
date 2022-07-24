package com.rakuishi.narou

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.rakuishi.narou.data.DataStoreRepository
import com.rakuishi.narou.data.NovelRepository
import com.rakuishi.narou.database.AppDatabase

class App : Application() {

    private lateinit var appDatabase: AppDatabase
    private val dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")

    lateinit var novelRepository: NovelRepository
    lateinit var dataStoreRepository: DataStoreRepository

    override fun onCreate() {
        super.onCreate()
        appDatabase = AppDatabase.getDatabase(this)
        novelRepository = NovelRepository(appDatabase.novelDao())
        dataStoreRepository = DataStoreRepository(dataStore)
    }
}