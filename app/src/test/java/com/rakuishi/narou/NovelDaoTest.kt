package com.rakuishi.narou

import androidx.room.Room
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
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class NovelDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: NovelDao

    @Before
    fun createDb() {
        val context = RuntimeEnvironment.getApplication()
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