package com.rakuishi.narou.repository

import androidx.annotation.VisibleForTesting
import com.rakuishi.narou.database.NovelDao
import com.rakuishi.narou.model.Novel
import com.rakuishi.narou.model.Site
import com.rakuishi.narou.util.await
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

class NovelRepository(
    private val dao: NovelDao,
    private val client: OkHttpClient,
) {

    suspend fun delete(id: Long) = dao.delete(id)

    suspend fun getItemById(id: Long): Novel? = dao.getItemById(id)

    suspend fun insertNewNovel(url: String): Novel? {
        return if (url.startsWith("https://ncode.syosetu.com/")) {
            val regex = Regex("""^https://ncode.syosetu.com/(n[a-z0-9]+)/$""")
            val match = regex.find(url) ?: return null
            val nid = match.groups[1]?.value ?: return null
            return fetchNarouNewNovel(nid)
        } else if (url.startsWith("https://kakuyomu.jp/works/")) {
            val regex = Regex("""^https://kakuyomu.jp/works/(\d+)$""")
            val match = regex.find(url) ?: return null
            val nid = match.groups[1]?.value ?: return null
            return fetchKakuyomuNewNovel(nid)
        } else {
            null
        }
    }

    private suspend fun fetchNarouNewNovel(nid: String) =
        withContext(Dispatchers.IO) {
            val url = "https://ncode.syosetu.com/${nid}/"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).await()
            val body = response.body?.string() ?: ""
            val novel = Novel(
                id = 0,
                title = "",
                authorName = "",
                site = Site.NCODE,
                nid = nid,
                latestEpisodeId = "0",
                latestEpisodeNumber = 0,
                latestEpisodeUpdatedAt = Date(),
                currentEpisodeId = "1",
                currentEpisodeNumber = 1,
                hasNewEpisode = false,
            )

            val titleRegex =
                Regex("""<meta property="og:title" content="(.+?)" />""")
            titleRegex.find(body)?.let {
                novel.title = it.groups[1]?.value ?: ""
            }

            val authorNameRegex =
                Regex("""<meta name="twitter:creator" content="(.+?)">""")
            authorNameRegex.find(body)?.let {
                novel.authorName = it.groups[1]?.value ?: ""
            }

            parseNarouEpisode(body)?.let {
                novel.currentEpisodeId = it.firstEpisodeId
                novel.currentEpisodeNumber = it.firstEpisodeNumber
                novel.latestEpisodeId = it.latestEpisodeId
                novel.latestEpisodeNumber = it.latestEpisodeNumber
                novel.latestEpisodeUpdatedAt = it.latestEpisodeUpdatedAt
            }

            return@withContext if (novel.title.isNotEmpty()
                && novel.authorName.isNotEmpty()
                && novel.latestEpisodeId.isNotEmpty()
            ) {
                novel.id = dao.insert(novel)
                novel
            } else {
                null
            }
        }

    private suspend fun fetchKakuyomuNewNovel(nid: String) =
        withContext(Dispatchers.IO) {
            val url = "https://kakuyomu.jp/works/${nid}"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).await()
            val body = response.body?.string() ?: ""
            val novel = Novel(
                id = 0,
                title = "",
                authorName = "",
                site = Site.KAKUYOMU,
                nid = nid,
                latestEpisodeId = "",
                latestEpisodeNumber = 0,
                latestEpisodeUpdatedAt = Date(),
                currentEpisodeId = "1",
                currentEpisodeNumber = 1,
                hasNewEpisode = false,
            )

            val titleRegex =
                Regex("""<h1 id="workTitle"><a href="/works/(\d+)">(.+?)</a></h1>""")
            titleRegex.find(body)?.let {
                novel.title = it.groups[2]?.value ?: ""
            }

            val authorNameRegex =
                Regex("""<span id="workAuthor-activityName"><a href="/users/(.+?)">(.+?)</a></span>""")
            authorNameRegex.find(body)?.let {
                novel.authorName = it.groups[2]?.value ?: ""
            }

            parseKakuyomuEpisode(body)?.let {
                novel.currentEpisodeId = it.firstEpisodeId
                novel.currentEpisodeNumber = it.firstEpisodeNumber
                novel.latestEpisodeId = it.latestEpisodeId
                novel.latestEpisodeNumber = it.latestEpisodeNumber
                novel.latestEpisodeUpdatedAt = it.latestEpisodeUpdatedAt
            }

            return@withContext if (novel.title.isNotEmpty()
                && novel.authorName.isNotEmpty()
                && novel.latestEpisodeId.isNotEmpty()
            ) {
                novel.id = dao.insert(novel)
                novel
            } else {
                null
            }
        }

    suspend fun fetchList(skipUpdateNewEpisode: Boolean): List<Novel> {
        val novels = dao.getList()

        if (!skipUpdateNewEpisode) {
            coroutineScope {
                novels.map { async { fetchNewEpisode(it) } }.awaitAll()
            }
        }

        return novels
    }

    suspend fun fetchNewEpisode(novel: Novel) =
        withContext(Dispatchers.IO) {
            val request = Request.Builder().url(novel.url).get().build()
            val response = client.newCall(request).await()
            val body = response.body?.string() ?: ""
            val episode = when (novel.site) {
                Site.NCODE -> parseNarouEpisode(body)
                Site.KAKUYOMU -> parseKakuyomuEpisode(body)
            } ?: return@withContext
            updateByLatestEpisodeNumberIfNeeded(
                novel = novel,
                episodes = episode
            )
        }

    private suspend fun updateByLatestEpisodeNumberIfNeeded(
        novel: Novel,
        episodes: Episodes,
    ) {
        if (episodes.latestEpisodeNumber > novel.latestEpisodeNumber
            || episodes.latestEpisodeUpdatedAt.after(novel.latestEpisodeUpdatedAt)
        ) {
            novel.latestEpisodeId = episodes.latestEpisodeId
            novel.latestEpisodeNumber = episodes.latestEpisodeNumber
            novel.latestEpisodeUpdatedAt = episodes.latestEpisodeUpdatedAt
            novel.hasNewEpisode = true
            dao.update(novel)
        }
    }

    class Episodes(
        val firstEpisodeId: String,
        val firstEpisodeNumber: Int,
        val latestEpisodeId: String,
        val latestEpisodeNumber: Int,
        val latestEpisodeUpdatedAt: Date,
    )

    private fun parseNarouEpisode(body: String): Episodes? {
        val regex =
            Regex("""<dd class="subtitle">\s+<a href="/n[a-z0-9]+/(\d+)/">.+?</a>\s+</dd>\s+<dt class="long_update">\s+(\d{4}/\d{2}/\d{2} \d{2}:\d{2})<""")
        regex.findAll(body).lastOrNull()?.let {
            val episodeNumber = it.groups[1]?.value?.toInt() ?: 0
            val updatedAtString = it.groups[2]?.value ?: ""
            val updatedAt: Date? = try {
                val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN)
                sdf.parse(updatedAtString)
            } catch (e: ParseException) {
                null
            }

            return Episodes(
                firstEpisodeId = "1",
                firstEpisodeNumber = 1,
                latestEpisodeId = episodeNumber.toString(),
                latestEpisodeNumber = episodeNumber,
                latestEpisodeUpdatedAt = updatedAt ?: Date()
            )
        }

        return null
    }

    private fun parseKakuyomuEpisode(body: String): Episodes? {
        val regex =
            Regex("""<li class="widget-toc-episode">\s+<a href="/works/\d+/episodes/(\d+)" class="widget-toc-episode-episodeTitle">\s+<span class="widget-toc-episode-titleLabel js-vertical-composition-item">(.+?)</span>\s+<time class="widget-toc-episode-datePublished" datetime="(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z)">(.+?)</time>\s+</a>\s+</li>""")
        regex.findAll(body).let { results ->
            val firstEpisodeId = results.firstOrNull()?.let {
                it.groups[1]?.value
            } ?: ""

            results.lastOrNull()?.let {
                val episodeId = it.groups[1]?.value ?: ""
                val episodeNumber = results.count()
                val updatedAtString = it.groups[3]?.value ?: ""
                val updatedAt: Date? = try {
                    Date(Instant.parse(updatedAtString).toEpochMilli() - 9 * 60 * 60 * 1000L)
                } catch (e: ParseException) {
                    null
                }

                return Episodes(
                    firstEpisodeId = firstEpisodeId,
                    firstEpisodeNumber = 1,
                    latestEpisodeId = episodeId,
                    latestEpisodeNumber = episodeNumber,
                    latestEpisodeUpdatedAt = updatedAt ?: Date()
                )
            }
        }

        return null
    }

    suspend fun updateCurrentEpisodeNumberIfMatched(url: String) {
        if (url.startsWith("https://ncode.syosetu.com/")) {
            updateNarouCurrentEpisodeNumberIfMatched(url)
        } else if (url.startsWith("https://kakuyomu.jp/")) {
            updateKakuyomuCurrentEpisodeNumberIfMatched(url)
        }
    }

    private suspend fun updateNarouCurrentEpisodeNumberIfMatched(url: String) {
        val regex = Regex("""^https://ncode.syosetu.com/(n[a-z0-9]+)/(\d+)/$""")
        val match = regex.find(url) ?: return
        val nid = match.groups[1]?.value ?: return
        val novel = dao.getItemByNid(nid) ?: return
        val episodeNumber = match.groups[2]?.value?.toInt() ?: return
        novel.currentEpisodeId = episodeNumber.toString()
        novel.currentEpisodeNumber = episodeNumber
        if (novel.hasNewEpisode && novel.latestEpisodeNumber == episodeNumber) {
            novel.hasNewEpisode = false
        }
        dao.update(novel)
    }

    private suspend fun updateKakuyomuCurrentEpisodeNumberIfMatched(url: String) {
        val regex = Regex("""^https://kakuyomu.jp/works/(\d+)/episodes/(\d+)$""")
        val match = regex.find(url) ?: return
        val nid = match.groups[1]?.value ?: return
        val novel = dao.getItemByNid(nid) ?: return
        val episodeId = match.groups[2]?.value ?: return
        val episodeNumber = fetchKakuyomuEpisodeNumber(nid, episodeId)
        novel.currentEpisodeId = episodeId
        novel.currentEpisodeNumber = episodeNumber
        if (novel.hasNewEpisode && novel.latestEpisodeNumber == episodeNumber) {
            novel.hasNewEpisode = false
        }
        dao.update(novel)
    }

    @VisibleForTesting
    suspend fun fetchKakuyomuEpisodeNumber(nid: String, episodeId: String): Int =
        withContext(Dispatchers.IO) {
            val url = "https://kakuyomu.jp/works/${nid}"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).await()
            val body = response.body?.string() ?: ""

            val regex =
                Regex("""<a href="/works/\d+/episodes/(\d+)" class="widget-toc-episode-episodeTitle">""")
            regex.findAll(body).forEachIndexed { index, result ->
                val id = result.groups[1]?.value ?: ""
                if (episodeId == id) return@withContext index + 1
            }

            return@withContext -1
        }
}