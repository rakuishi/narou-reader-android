package com.rakuishi.nreader.repository

import androidx.annotation.VisibleForTesting
import com.rakuishi.nreader.database.NovelDao
import com.rakuishi.nreader.model.Novel
import com.rakuishi.nreader.model.Site
import com.rakuishi.nreader.util.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date
import java.util.Locale

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

    private suspend fun fetchNarouNewNovel(nid: String): Novel? =
        withContext(Dispatchers.IO) {
            val url = "https://ncode.syosetu.com/novelview/infotop/ncode/${nid}/"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).await()
            val body = response.body?.string() ?: ""
            val info = parseNarouInfo(body) ?: return@withContext null

            val novel = Novel(
                id = 0,
                title = info.title,
                authorName = info.authorName,
                site = Site.NCODE,
                nid = nid,
                latestEpisodeId = info.latestEpisodeId,
                latestEpisodeNumber = info.latestEpisodeNumber,
                latestEpisodeUpdatedAt = info.latestEpisodeUpdatedAt,
                currentEpisodeId = info.firstEpisodeId,
                currentEpisodeNumber = info.firstEpisodeNumber,
                hasNewEpisode = false,
            )

            novel.id = dao.insert(novel)
            return@withContext novel
        }

    private suspend fun fetchKakuyomuNewNovel(nid: String): Novel? =
        withContext(Dispatchers.IO) {
            val url = "https://kakuyomu.jp/works/${nid}"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).await()
            val body = response.body?.string() ?: ""
            val info = parseKakuyomuInfo(body) ?: return@withContext null

            val novel = Novel(
                id = 0,
                title = info.title,
                authorName = info.authorName,
                site = Site.KAKUYOMU,
                nid = nid,
                latestEpisodeId = info.latestEpisodeId,
                latestEpisodeNumber = info.latestEpisodeNumber,
                latestEpisodeUpdatedAt = info.latestEpisodeUpdatedAt,
                currentEpisodeId = info.firstEpisodeId,
                currentEpisodeNumber = info.firstEpisodeNumber,
                hasNewEpisode = false,
            )

            novel.id = dao.insert(novel)
            return@withContext novel
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
                Site.NCODE -> parseNarouInfo(body)
                Site.KAKUYOMU -> parseKakuyomuInfo(body)
            } ?: return@withContext
            updateByLatestEpisodeNumberIfNeeded(
                novel = novel,
                novelInfo = episode
            )
        }

    private suspend fun updateByLatestEpisodeNumberIfNeeded(
        novel: Novel,
        novelInfo: NovelInfo,
    ) {
        if (novelInfo.latestEpisodeNumber > novel.latestEpisodeNumber
            || novelInfo.latestEpisodeUpdatedAt.after(novel.latestEpisodeUpdatedAt)
        ) {
            novel.latestEpisodeId = novelInfo.latestEpisodeId
            novel.latestEpisodeNumber = novelInfo.latestEpisodeNumber
            novel.latestEpisodeUpdatedAt = novelInfo.latestEpisodeUpdatedAt
            novel.hasNewEpisode = true
            dao.update(novel)
        }
    }

    class NovelInfo(
        val title: String,
        val authorName: String,
        val firstEpisodeId: String,
        val firstEpisodeNumber: Int,
        val latestEpisodeId: String,
        val latestEpisodeNumber: Int,
        val latestEpisodeUpdatedAt: Date,
    )

    private fun parseNarouInfo(body: String): NovelInfo? {
        var title = ""
        val titleRegex =
            Regex("""<meta property="og:title" content="(.+?)" />""")
        titleRegex.find(body)?.let {
            title = it.groups[1]?.value ?: ""
        }

        var authorName = ""
        val authorNameRegex =
            Regex("""<meta name="twitter:creator" content="(.+?)">""")
        authorNameRegex.find(body)?.let {
            authorName = it.groups[1]?.value ?: ""
        }

        var episodeNumber = 0
        val episodeNumberRegex =
            Regex("""</span>全(\d+)部分""")
        episodeNumberRegex.find(body)?.let {
            episodeNumber = it.groups[1]?.value?.toInt() ?: 0
        }

        var updatedAt = Date()
        val updatedAtRegex =
            Regex("""<th>最新部分掲載日</th>\s+<td>(\d{4}年 \d{2}月\d{2}日 \d{2}時\d{2}分)</td>""")
        updatedAtRegex.find(body)?.let {
            val updatedAtString = it.groups[1]?.value ?: ""
            updatedAt = try {
                val sdf = SimpleDateFormat("yyyy年 MM月dd日 HH時mm分", Locale.JAPAN)
                sdf.parse(updatedAtString) ?: Date()
            } catch (e: ParseException) {
                Date()
            }
        }

        return if (title.isNotEmpty()) {
            NovelInfo(
                title = title,
                authorName = authorName,
                firstEpisodeId = "1",
                firstEpisodeNumber = 1,
                latestEpisodeId = episodeNumber.toString(),
                latestEpisodeNumber = episodeNumber,
                latestEpisodeUpdatedAt = updatedAt,
            )
        } else {
            null
        }
    }

    private fun parseKakuyomuInfo(body: String): NovelInfo? {
        var title = ""
        val titleRegex =
            Regex(""""__typename":"Work","id":"\d+","title":"(.+?)"""")
        titleRegex.find(body)?.let {
            title = it.groups[1]?.value ?: ""
        }

        var authorName = ""
        val authorNameRegex =
            Regex(""""activityName":"(.+?)"""")
        authorNameRegex.find(body)?.let {
            authorName = it.groups[1]?.value ?: ""
        }

        var firstEpisodeId: String
        var latestEpisodeId = ""
        var latestEpisodeNumber = 0
        var latestEpisodeUpdatedAt = Date()
        val regex =
            Regex(""""__typename":"Episode","id":"(\d+)","title":".+?","publishedAt":"(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})Z"""")
        regex.findAll(body).let { results ->
            firstEpisodeId = results.firstOrNull()?.let {
                it.groups[1]?.value
            } ?: ""

            results.lastOrNull()?.let {
                latestEpisodeId = it.groups[1]?.value ?: ""
                latestEpisodeNumber = results.count()

                val updatedAt = ZonedDateTime.of(
                    it.groups[2]?.value?.toInt() ?: 0,
                    it.groups[3]?.value?.toInt() ?: 0,
                    it.groups[4]?.value?.toInt() ?: 0,
                    it.groups[5]?.value?.toInt() ?: 0,
                    it.groups[6]?.value?.toInt() ?: 0,
                    it.groups[7]?.value?.toInt() ?: 0,
                    0,
                    ZoneId.of("GMT0")
                )
                latestEpisodeUpdatedAt = Date(updatedAt.toEpochSecond() * 1000)
            }
        }

        return if (title.isNotEmpty()) {
            NovelInfo(
                title = title,
                authorName = authorName,
                firstEpisodeId = firstEpisodeId,
                firstEpisodeNumber = 1,
                latestEpisodeId = latestEpisodeId,
                latestEpisodeNumber = latestEpisodeNumber,
                latestEpisodeUpdatedAt = latestEpisodeUpdatedAt,
            )
        } else {
            null
        }
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
                Regex(""""__typename":"Episode","id":"(\d+)","title":".+?","publishedAt":"(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})Z"""")
            regex.findAll(body).forEachIndexed { index, result ->
                val id = result.groups[1]?.value ?: ""
                if (episodeId == id) return@withContext index + 1
            }

            return@withContext -1
        }
}