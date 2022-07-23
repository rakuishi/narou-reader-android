package com.rakuishi.narou.data

import com.rakuishi.narou.database.NovelDao
import com.rakuishi.narou.model.Novel
import com.rakuishi.narou.util.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class NovelRepository(private val dao: NovelDao) {

    suspend fun getItemById(id: Int): Novel? = dao.getItemById(id)

    suspend fun fetchList(): List<Novel> {
        val client = OkHttpClient()
        val novels = dao.getList()

        novels.forEach { novel ->
            fetchNewEpisodeFromNarouServer(novel, client)
        }

        return novels
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun fetchNewEpisodeFromNarouServer(novel: Novel, client: OkHttpClient) =
        withContext(Dispatchers.IO) {
            val request = Request.Builder().url(novel.url).get().build()
            val response = client.newCall(request).await()
            val body = response.body?.string() ?: ""
            val regex =
                Regex("""<dd class="subtitle">\s+<a href="/${novel.nid}/(\d+)/">.+?</a>\s+</dd>\s+<dt class="long_update">\s+(\d{4}/\d{2}/\d{2} \d{2}:\d{2})</dt>""")
            val match = regex.findAll(body)

            match.lastOrNull()?.let {
                val episodeNumber = it.groups[1]?.value?.toInt() ?: 0
                val updatedAtString = it.groups[2]?.value ?: ""
                updateByLatestEpisodeNumberIfNeeded(novel, episodeNumber, updatedAtString)
            }
        }

    private suspend fun updateByLatestEpisodeNumberIfNeeded(
        novel: Novel,
        episodeNumber: Int,
        updatedAtString: String
    ) {
        val updatedAt: Date = try {
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN)
            sdf.parse(updatedAtString)
        } catch (e: ParseException) {
            null
        } ?: return

        if (episodeNumber > novel.latestEpisodeNumber || updatedAt.after(novel.latestEpisodeUpdatedAt)) {
            novel.latestEpisodeNumber = episodeNumber
            novel.latestEpisodeUpdatedAt = updatedAt
            novel.hasNewEpisode = true
            dao.update(novel)
        }
    }

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