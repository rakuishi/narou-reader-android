package com.rakuishi.narou.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "novels")
data class Novel(
    @PrimaryKey(autoGenerate = true) var id: Long,
    @ColumnInfo(name = "nid") var nid: String,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "author_name") var authorName: String,
    @ColumnInfo(name = "latest_episode_number") var latestEpisodeNumber: Int,
    @ColumnInfo(name = "latest_episode_updated_at") var latestEpisodeUpdatedAt: Date,
    @ColumnInfo(name = "current_episode_number") var currentEpisodeNumber: Int,
    @ColumnInfo(name = "has_new_episode") var hasNewEpisode: Boolean,
) {

    val url: String
        get() = "https://ncode.syosetu.com/${nid}/"

    val latestEpisodeUrl: String
        get() = "https://ncode.syosetu.com/${nid}/${latestEpisodeNumber}/"

    fun getEpisodeUrl(episodeNumber: Int): String =
        "https://ncode.syosetu.com/${nid}/${episodeNumber}/"
}