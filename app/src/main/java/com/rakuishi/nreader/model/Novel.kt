package com.rakuishi.nreader.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "novels")
data class Novel(
    @PrimaryKey(autoGenerate = true) var id: Long,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "author_name") var authorName: String,
    @ColumnInfo(name = "site") var site: Site, // ncode, kakuyomu
    @ColumnInfo(name = "nid") var nid: String,
    @ColumnInfo(name = "latest_episode_id") var latestEpisodeId: String,
    @ColumnInfo(name = "latest_episode_number") var latestEpisodeNumber: Int,
    @ColumnInfo(name = "latest_episode_updated_at") var latestEpisodeUpdatedAt: Date,
    @ColumnInfo(name = "current_episode_id") var currentEpisodeId: String,
    @ColumnInfo(name = "current_episode_number") var currentEpisodeNumber: Int,
    @ColumnInfo(name = "has_new_episode") var hasNewEpisode: Boolean,
) {

    val url: String
        get() = when (site) {
            Site.NCODE -> "https://ncode.syosetu.com/novelview/infotop/ncode/${nid}/"
            Site.KAKUYOMU -> "https://kakuyomu.jp/works/${nid}"
        }

    val latestEpisodeUrl: String
        get() = when (site) {
            Site.NCODE -> "https://ncode.syosetu.com/${nid}/${latestEpisodeId}/"
            Site.KAKUYOMU -> "https://kakuyomu.jp/works/${nid}/episodes/${latestEpisodeId}"
        }

    fun getEpisodeUrl(episodeId: String): String = when (site) {
        Site.NCODE -> "https://ncode.syosetu.com/${nid}/${episodeId}/"
        Site.KAKUYOMU -> "https://kakuyomu.jp/works/${nid}/episodes/${episodeId}"
    }
}