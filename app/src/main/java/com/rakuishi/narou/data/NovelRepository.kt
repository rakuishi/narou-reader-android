package com.rakuishi.narou.data

import com.rakuishi.narou.database.NovelDao
import com.rakuishi.narou.model.Novel

class NovelRepository(private val dao: NovelDao) {

    suspend fun insert(novel: Novel) = dao.insert(novel)

    suspend fun update(novel: Novel) = dao.update(novel)

    suspend fun getItemById(id: Int): Novel? = dao.getItemById(id)

    suspend fun getList(): List<Novel> = dao.getList()
}