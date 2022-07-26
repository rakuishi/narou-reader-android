package com.rakuishi.narou

import androidx.room.Room
import com.rakuishi.narou.database.AppDatabase
import com.rakuishi.narou.model.Novel
import com.rakuishi.narou.repository.NovelRepository
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.*

@RunWith(RobolectricTestRunner::class)
class NovelRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var novelRepository: NovelRepository
    private val okHttpClient = OkHttpClient()

    @Before
    fun setupRepository() {
        val context = RuntimeEnvironment.getApplication()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        novelRepository = NovelRepository(database.novelDao(), okHttpClient)
    }

    @Test
    fun insertNewNovelByUrl() = runBlocking {
        val novel = novelRepository.insertNewNovel("https://ncode.syosetu.com/n9669bk/")
        assertEquals("無職転生　- 異世界行ったら本気だす -", novel?.title)
        assertEquals("理不尽な孫の手", novel?.authorName)
    }

    @Test
    fun insertNewNovelByNid() = runBlocking {
        val novel = novelRepository.insertNewNovel("n9669bk")
        assertEquals("無職転生　- 異世界行ったら本気だす -", novel?.title)
        assertEquals("理不尽な孫の手", novel?.authorName)
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
        novelRepository.fetchNewEpisodeFromNarouServer(novel)
        assertEquals(novel.latestEpisodeNumber, 286)
    }

    @Test
    fun updateCurrentEpisodeNumberIfMatched() = runBlocking {
        val novel1 = novelRepository.insertNewNovel("https://ncode.syosetu.com/n4811fg/") as Novel
        assertEquals(1, novel1.id)
        assertEquals("n4811fg", novel1.nid)
        assertEquals(1, novel1.currentEpisodeNumber)

        val url = "https://ncode.syosetu.com/${novel1.nid}/2/"
        novelRepository.updateCurrentEpisodeNumberIfMatched(url)

        val novel2 = novelRepository.getItemById(novel1.id) as Novel
        assertEquals(2, novel2.currentEpisodeNumber)
    }
}