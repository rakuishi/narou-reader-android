package com.rakuishi.narou.data

import com.rakuishi.narou.database.NovelDao
import com.rakuishi.narou.model.Novel

class NovelRepository(private val dao: NovelDao) {

    suspend fun insert(novel: Novel) = dao.insert(novel)

    suspend fun update(novel: Novel) = dao.update(novel)

    suspend fun getItemById(id: Int): Novel? = dao.getItemById(id)

    suspend fun getList(): List<Novel> = dao.getList()

    suspend fun updateCurrentEpisodeNumberIfMatched(url: String) {
        val regex = Regex("""^https://ncode.syosetu.com/([a-z0-9]+)/(\d+)/$""")
        val match = regex.find(url) ?: return
        val nid = match.groups[1]?.value ?: return
        val novel = dao.getItemByNid(nid) ?: return
        val episodeNumber = match.groups[2]?.value?.toInt() ?: return
        novel.currentEpisodeNumber = episodeNumber
        dao.update(novel)
    }
}