package com.rakuishi.nreader

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.Configuration
import com.rakuishi.nreader.database.AppDatabase
import com.rakuishi.nreader.repository.DataStoreRepository
import com.rakuishi.nreader.repository.NovelRepository
import com.rakuishi.nreader.util.NotificationHelper
import com.rakuishi.nreader.worker.NewEpisodeWorker
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android

class App : Application(), Configuration.Provider {

    private lateinit var appDatabase: AppDatabase
    private val dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")
    private val httpClient = HttpClient(Android)

    // TODO: Use dependency injection in the future
    lateinit var novelRepository: NovelRepository
    lateinit var dataStoreRepository: DataStoreRepository

    override fun onCreate() {
        super.onCreate()
        appDatabase = AppDatabase.getDatabase(this)
        novelRepository = NovelRepository(appDatabase.novelDao(), httpClient)
        dataStoreRepository = DataStoreRepository(dataStore)

        NotificationHelper.setupNotificationChannel(this)
        NewEpisodeWorker.enqueue(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()
}