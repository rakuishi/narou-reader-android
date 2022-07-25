package com.rakuishi.narou

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rakuishi.narou.data.NovelRepository
import com.rakuishi.narou.database.AppDatabase
import com.rakuishi.narou.model.Novel
import com.rakuishi.narou.util.SampleDataProvider
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class NovelRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var novelRepository: NovelRepository
    private val okHttpClient = OkHttpClient()

    @Before
    fun setupRepository() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        novelRepository = NovelRepository(database.novelDao())
    }

    @Test
    fun fetchNewEpisodeFromNarouServer() = runBlocking {
        // 完結済
        val novel = Novel(
            id = 1,
            nid = "n9669bk",
            title = "無職転生　- 異世界行ったら本気だす -",
            authorName = "理不尽な孫の手",
            latestEpisodeNumber = 1,
            latestEpisodeUpdatedAt = Date(1353603600000),
            currentEpisodeNumber = 1,
            hasNewEpisode = false,
        )
        novelRepository.fetchNewEpisodeFromNarouServer(novel, okHttpClient)
        assertEquals(novel.latestEpisodeNumber, 286)
    }

    @Test
    fun updateCurrentEpisodeNumberIfMatched() = runBlocking {
        val novel1 = SampleDataProvider.novel()
        novelRepository.insert(novel1)
        assertEquals(novel1.currentEpisodeNumber, 1)

        val url = "https://ncode.syosetu.com/${novel1.nid}/2/"
        novelRepository.updateCurrentEpisodeNumberIfMatched(url)

        val novel2 = novelRepository.getItemById(novel1.id) as Novel
        assertEquals(novel2.currentEpisodeNumber, 2)
    }
}