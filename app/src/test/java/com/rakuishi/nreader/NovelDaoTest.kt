package com.rakuishi.nreader

import androidx.room.Room
import com.rakuishi.nreader.database.AppDatabase
import com.rakuishi.nreader.database.NovelDao
import com.rakuishi.nreader.model.Novel
import com.rakuishi.nreader.util.SampleDataProvider
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