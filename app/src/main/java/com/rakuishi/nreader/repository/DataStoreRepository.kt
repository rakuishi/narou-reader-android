package com.rakuishi.nreader.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.map

class DataStoreRepository(
    private val dataStore: DataStore<Preferences>
) {

    private val cookieKeys: Array<String> = arrayOf(
        "lineheight",
        "fontsize",
        "smpfontsize",
        "smpnovellayout",
        "novellayout",
        "night_mode",
        "smplineheight",
        "fix_menu_bar",
        "sasieno"
    )

    fun readCookies() = dataStore.data.map { it ->
        val map: MutableMap<String, String> = mutableMapOf()
        for (key in cookieKeys) {
            it[stringPreferencesKey(key)]?.let { map[key] = it }
        }
        map
    }

    suspend fun saveCookies(url: String, cookiesString: String) {
        if (!url.startsWith("https://ncode.syosetu.com")) return
        val cookies = cookiesString.split("; ")

        dataStore.edit { preferences ->
            for (cookieString in cookies) {
                val pair = cookieString.split("=")
                val key = pair.first()
                if (cookieKeys.contains(key)) {
                    preferences[stringPreferencesKey(key)] = pair.last()
                }
            }
        }
    }
}