package com.rakuishi.narou

import androidx.room.Room
import com.rakuishi.narou.database.AppDatabase
import com.rakuishi.narou.model.Novel
import com.rakuishi.narou.model.Site
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
    fun insertNarouNewNovel() = runBlocking {
        val novel = novelRepository.insertNewNovel("https://ncode.syosetu.com/n9669bk/")
        assertEquals("無職転生　- 異世界行ったら本気だす -", novel?.title)
        assertEquals("理不尽な孫の手", novel?.authorName)
        assertEquals(Site.NCODE, novel?.site)
        assertEquals("1", novel?.currentEpisodeId)
        assertEquals(1, novel?.currentEpisodeNumber)
        assertEquals("286", novel?.latestEpisodeId)
        assertEquals(286, novel?.latestEpisodeNumber)
    }

    @Test
    fun insertKakuyomuNewNovel() = runBlocking {
        val novel = novelRepository.insertNewNovel("https://kakuyomu.jp/works/1177354054882739112")
        assertEquals("ひげを剃る。そして女子高生を拾う。", novel?.title)
        assertEquals("しめさば", novel?.authorName)
        assertEquals(Site.KAKUYOMU, novel?.site)
        assertEquals("1177354054882739226", novel?.currentEpisodeId.toString())
        assertEquals(1, novel?.currentEpisodeNumber)
        assertEquals("1177354054886577614", novel?.latestEpisodeId.toString())
        assertEquals(39, novel?.latestEpisodeNumber)
    }

    @Test
    fun fetchNarouNewEpisode() = runBlocking {
        val novel = Novel(
            id = 1,
            title = "無職転生　- 異世界行ったら本気だす -",
            authorName = "理不尽な孫の手",
            site = Site.NCODE,
            nid = "n9669bk",
            latestEpisodeId = "1",
            latestEpisodeNumber = 1,
            latestEpisodeUpdatedAt = Date(1353603600000),
            currentEpisodeId = "1",
            currentEpisodeNumber = 1,
            hasNewEpisode = false,
        )
        novelRepository.fetchNewEpisode(novel)
        assertEquals(286, novel.latestEpisodeNumber)
    }

    @Test
    fun fetchKakuyomuNewEpisode() = runBlocking {
        val novel = Novel(
            id = 1,
            title = "ひげを剃る。そして女子高生を拾う。",
            authorName = "しめさば",
            site = Site.KAKUYOMU,
            nid = "1177354054882739112",
            latestEpisodeId = "1",
            latestEpisodeNumber = 1,
            latestEpisodeUpdatedAt = Date(1488965753000),
            currentEpisodeId = "1",
            currentEpisodeNumber = 1,
            hasNewEpisode = false,
        )
        novelRepository.fetchNewEpisode(novel)
        assertEquals(39, novel.latestEpisodeNumber)
    }

    @Test
    fun updateNarouCurrentEpisodeNumberIfMatched() = runBlocking {
        val novel1 = novelRepository.insertNewNovel("https://ncode.syosetu.com/n4811fg/") as Novel
        assertEquals(1, novel1.id)
        assertEquals("n4811fg", novel1.nid)
        assertEquals("1", novel1.currentEpisodeId)
        assertEquals(1, novel1.currentEpisodeNumber)

        val url = "https://ncode.syosetu.com/n4811fg/2/"
        novelRepository.updateCurrentEpisodeNumberIfMatched(url)

        val novel2 = novelRepository.getItemById(novel1.id) as Novel
        assertEquals("2", novel2.currentEpisodeId)
        assertEquals(2, novel2.currentEpisodeNumber)
    }

    @Test
    fun updateKakuyomuCurrentEpisodeNumberIfMatched() = runBlocking {
        val novel1 =
            novelRepository.insertNewNovel("https://kakuyomu.jp/works/1177354054882739112") as Novel
        assertEquals(1, novel1.id)
        assertEquals("1177354054882739112", novel1.nid)
        assertEquals("1177354054882739226", novel1.currentEpisodeId)
        assertEquals(1, novel1.currentEpisodeNumber)

        val url = "https://kakuyomu.jp/works/1177354054882739112/episodes/1177354054882741427"
        novelRepository.updateCurrentEpisodeNumberIfMatched(url)

        val novel2 = novelRepository.getItemById(novel1.id) as Novel
        assertEquals("1177354054882741427", novel2.currentEpisodeId)
        assertEquals(2, novel2.currentEpisodeNumber)
    }

    @Test
    fun fetchKakuyomuEpisodeNumber() = runBlocking {
        // episode2 -> https://kakuyomu.jp/works/1177354054882739112/episodes/1177354054882741427
        val episodeNumber = novelRepository.fetchKakuyomuEpisodeNumber(
            nid = "1177354054882739112",
            episodeId = "1177354054882741427"
        )
        assertEquals(2, episodeNumber)
    }
}