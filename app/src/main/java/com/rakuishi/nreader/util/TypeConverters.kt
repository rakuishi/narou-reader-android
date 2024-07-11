package com.rakuishi.nreader.util

import androidx.room.TypeConverter
import com.rakuishi.nreader.model.Site
import java.util.Date

class TypeConverter {

    @TypeConverter
    fun longToDate(value: Long?): Date? = value?.let { Date(value) }

    @TypeConverter
    fun dateToLong(date: Date?): Long? = date?.time

    @TypeConverter
    fun stringToSite(value: String?): Site? =
        value?.let { Site.entries.firstOrNull { it.value == value } }

    @TypeConverter
    fun siteToString(value: Site?): String? = value?.value
}