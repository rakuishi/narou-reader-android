package com.rakuishi.nreader.util

import android.content.res.Resources

object LocaleUtil {

    fun isJa(): Boolean {
        return Resources.getSystem().configuration.locales[0].language.contains("ja")
    }
}