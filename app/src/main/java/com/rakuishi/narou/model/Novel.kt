package com.rakuishi.narou.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "novels")
data class Novel(
    @PrimaryKey(autoGenerate = true) var id: Long,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "author_name") var authorName: String,
    @ColumnInfo(name = "domain") var domain: String, // ncode, kakuyomu
    @ColumnInfo(name = "nid") var nid: String,
    @ColumnInfo(name = "latest_episode_id") var latestEpisodeId: String,
    @ColumnInfo(name = "latest_episode_number") var latestEpisodeNumber: Int,
    @ColumnInfo(name = "latest_episode_updated_at") var latestEpisodeUpdatedAt: Date,
    @ColumnInfo(name = "current_episode_id") var currentEpisodeId: String,
    @ColumnInfo(name = "current_episode_number") var currentEpisodeNumber: Int,
    @ColumnInfo(name = "has_new_episode") var hasNewEpisode: Boolean,
) {

    companion object {
        const val DOMAIN_NCODE = "ncode"
        const val DOMAIN_KAKUYOMU = "kakuyomu"
    }

    val url: String
        get() = when (domain) {
            DOMAIN_NCODE -> "https://ncode.syosetu.com/${nid}/"
            DOMAIN_KAKUYOMU -> "https://kakuyomu.jp/works/${nid}"
            else -> throw IllegalStateException("domain must be set.")
        }

    val latestEpisodeUrl: String
        get() = when (domain) {
            DOMAIN_NCODE -> "https://ncode.syosetu.com/${nid}/${latestEpisodeId}/"
            DOMAIN_KAKUYOMU -> "https://kakuyomu.jp/works/${nid}/episodes/${latestEpisodeId}"
            else -> throw IllegalStateException("domain must be set.")
        }

    fun getEpisodeUrl(episodeId: String): String = when (domain) {
        DOMAIN_NCODE -> "https://ncode.syosetu.com/${nid}/${episodeId}/"
        DOMAIN_KAKUYOMU -> "https://kakuyomu.jp/works/${nid}/episodes/${episodeId}"
        else -> throw IllegalStateException("domain must be set.")
    }
}