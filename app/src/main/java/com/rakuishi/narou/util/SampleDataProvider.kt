package com.rakuishi.narou.util

import com.rakuishi.narou.model.Novel
import java.util.*

object SampleDataProvider {

    fun sql(): String = "INSERT INTO novels VALUES " +
            "(NULL, 'n4811fg', 'TRPGプレイヤーが異世界で最強ビルドを目指す～ヘンダーソン氏の福音を～', 'Schuld', 242, 1658484000000, 1, 0)," +
            "(NULL, 'n3556o', 'Knight''s & Magic', '天酒之瓢', 204, 1656552600000, 1, 0)," +
            "(NULL, 'n5881cl', '賢者の孫', '吉岡剛', 299, 1656987360000, 1, 0)"

    fun novel(): Novel = Novel(
        id = 0,
        nid = "n4811fg",
        title = "TRPGプレイヤーが異世界で最強ビルドを目指す～ヘンダーソン氏の福音を～",
        authorName = "Schuld",
        latestEpisodeNumber = 242,
        latestEpisodeUpdatedAt = Date(1658484000000),
        currentEpisodeNumber = 1,
        hasNewEpisode = true,
    )
}