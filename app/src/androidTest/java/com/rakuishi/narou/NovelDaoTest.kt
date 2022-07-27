package com.rakuishi.narou

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rakuishi.narou.database.AppDatabase
import com.rakuishi.narou.database.NovelDao
import com.rakuishi.narou.model.Novel
import com.rakuishi.narou.util.SampleDataProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

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
        dao.insert(SampleDataProvider.novel())
        val novel1 = dao.getItemById(1) as Novel
        assertEquals(1, novel1.id)

        dao.insert(SampleDataProvider.novel())
        val novel2 = dao.getItemById(2) as Novel
        assertEquals(2, novel2.id)
    }

    @Test
    fun deleteNovel() = runBlocking {
        dao.insert(SampleDataProvider.novel())
        assertTrue(dao.getItemById(1) != null)
        dao.delete(1)
        assertTrue(dao.getItemById(1) == null)
    }
}