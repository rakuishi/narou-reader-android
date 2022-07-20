package com.rakuishi.narou

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rakuishi.narou.database.AppDatabase
import com.rakuishi.narou.database.NovelDao
import com.rakuishi.narou.model.Novel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class NovelDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: NovelDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = database.novelDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertNovel() = runBlocking {
        val novel = Novel(
            id = 1,
            nid = "n4811fg",
            title = "TRPGプレイヤーが異世界で最強ビルドを目指す～ヘンダーソン氏の福音を～",
            authorName = "Schuld",
            latestEpisodeNumber = 241,
            latestEpisodeUpdatedAt = Date(),
            currentEpisodeNumber = 1,
            hasNewEpisode = true,
        )
        dao.insert(novel)
        assertEquals(dao.getItemById(1)?.nid, "n4811fg")
    }
}