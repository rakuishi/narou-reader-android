package com.rakuishi.narou

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.rakuishi.narou.database.AppDatabase
import com.rakuishi.narou.repository.DataStoreRepository
import com.rakuishi.narou.repository.NovelRepository
import okhttp3.OkHttpClient

class App : Application() {

    private lateinit var appDatabase: AppDatabase
    private val dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")
    private val okHttpClient = OkHttpClient()

    // TODO: Use dependency injection in the future
    lateinit var novelRepository: NovelRepository
    lateinit var dataStoreRepository: DataStoreRepository

    override fun onCreate() {
        super.onCreate()
        appDatabase = AppDatabase.getDatabase(this)
        novelRepository = NovelRepository(appDatabase.novelDao(), okHttpClient)
        dataStoreRepository = DataStoreRepository(dataStore)
    }
}