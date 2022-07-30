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

    private val titleRegex =
        Regex("""<meta property="og:title" content="(.+?)" />""")
    private val authorNameRegex =
        Regex("""<meta name="twitter:creator" content="(.+?)">""")

    private fun latestEpisodeRegex(nid: String): Regex =
        Regex("""<dd class="subtitle">\s+<a href="/${nid}/(\d+)/">.+?</a>\s+</dd>\s+<dt class="long_update">\s+(\d{4}/\d{2}/\d{2} \d{2}:\d{2})<""")

    suspend fun delete(id: Long) = dao.delete(id)

    suspend fun getItemById(id: Long): Novel? = dao.getItemById(id)

    suspend fun insertNewNovel(urlOrNid: String): Novel? {
        val regex = if (urlOrNid.startsWith("https://")) {
            Regex("""^https://ncode.syosetu.com/(n[a-z0-9]+)/$""")
        } else {
            Regex("""^(n[a-z0-9]+)$""")
        }
        val match = regex.find(urlOrNid) ?: return null
        val nid = match.groups[1]?.value ?: return null
        return fetchNewNovelFromNarouServer(nid)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun fetchNewNovelFromNarouServer(nid: String) =
        withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val url = "https://ncode.syosetu.com/${nid}/"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).await()
            val body = response.body?.string() ?: ""
            val novel = Novel(
                id = 0,
                nid = nid,
                title = "",
                authorName = "",
                latestEpisodeNumber = 0,
                latestEpisodeUpdatedAt = Date(),
                currentEpisodeNumber = 1,
                hasNewEpisode = false,
            )

            titleRegex.find(body)?.let {
                novel.title = it.groups[1]?.value ?: ""
            }

            authorNameRegex.find(body)?.let {
                novel.authorName = it.groups[1]?.value ?: ""
            }

            latestEpisodeRegex(novel.nid).findAll(body).lastOrNull()?.let { it ->
                novel.latestEpisodeNumber = it.groups[1]?.value?.toInt() ?: 0

                val updatedAtString = it.groups[2]?.value ?: ""
                getDateByUpdatedAtString(updatedAtString)?.let { updatedAt ->
                    novel.latestEpisodeUpdatedAt = updatedAt
                }
            }

            return@withContext if (novel.title.isNotEmpty()
                && novel.authorName.isNotEmpty()
                && novel.latestEpisodeNumber > 0
            ) {
                novel.id = dao.insert(novel)
                novel
            } else {
                null
            }
        }

    suspend fun fetchList(skipUpdateNewEpisode: Boolean): List<Novel> {
        val client = OkHttpClient()
        val novels = dao.getList()

        if (!skipUpdateNewEpisode) {
            novels.forEach { novel ->
                fetchNewEpisodeFromNarouServer(novel, client)
            }
        }

        return novels
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun fetchNewEpisodeFromNarouServer(novel: Novel, client: OkHttpClient) =
        withContext(Dispatchers.IO) {
            val request = Request.Builder().url(novel.url).get().build()
            val response = client.newCall(request).await()
            val body = response.body?.string() ?: ""

            latestEpisodeRegex(novel.nid).findAll(body).lastOrNull()?.let {
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
        val updatedAt: Date = getDateByUpdatedAtString(updatedAtString) ?: return

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

    suspend fun consumeHasNewEpisodeIfNeeded(novel: Novel) {
        if (novel.hasNewEpisode) {
            novel.hasNewEpisode = false
            dao.update(novel)
        }
    }

    private fun getDateByUpdatedAtString(updatedAtString: String): Date? {
        return try {
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN)
            sdf.parse(updatedAtString)
        } catch (e: ParseException) {
            null
        }
    }
}