package com.rakuishi.narou.util

import com.rakuishi.narou.model.Novel
import com.rakuishi.narou.model.Site
import java.util.*

object SampleDataProvider {

    fun sql(): String = "INSERT INTO novels VALUES " +
            "(NULL, 'TRPGプレイヤーが異世界で最強ビルドを目指す～ヘンダーソン氏の福音を～', 'Schuld', 'ncode', 'n4811fg', '242', 242, 1658484000000, '1', 1, 0)," +
            "(NULL, 'Knight''s & Magic', '天酒之瓢', 'ncode', 'n3556o', '204', 204, 1656552600000, '1', 1, 0)," +
            "(NULL, '魔王のあとつぎ', '吉岡剛', 'kakuyomu', '16817139556372190893', '16817330648546731561', 23, 1670059337000, '16817139556372653523', 1, 0)"

    fun novel(): Novel = Novel(
        id = 0,
        title = "TRPGプレイヤーが異世界で最強ビルドを目指す～ヘンダーソン氏の福音を～",
        authorName = "Schuld",
        site = Site.NCODE,
        nid = "n4811fg",
        latestEpisodeId = "242",
        latestEpisodeNumber = 242,
        latestEpisodeUpdatedAt = Date(1658484000000),
        currentEpisodeId = "1",
        currentEpisodeNumber = 1,
        hasNewEpisode = true,
    )
}