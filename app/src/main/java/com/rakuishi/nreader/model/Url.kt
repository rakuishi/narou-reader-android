package com.rakuishi.nreader.model

object Url {

    const val NAROU = "https://ncode.syosetu.com"
    const val KAKUYOMU = "https://kakuyomu.jp/works"

    fun getNarouFetchUrl(nid: String): String = "$NAROU/novelview/infotop/ncode/${nid}/"
    fun getKakuyomuFetchUrl(nid: String): String = "$KAKUYOMU/${nid}"
}