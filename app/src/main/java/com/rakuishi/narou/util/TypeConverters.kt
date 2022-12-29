package com.rakuishi.narou.util

import androidx.room.TypeConverter
import com.rakuishi.narou.model.Site
import java.util.*

class TypeConverter {

    @TypeConverter
    fun longToDate(value: Long?): Date? = value?.let { Date(value) }

    @TypeConverter
    fun dateToLong(date: Date?): Long? = date?.time

    @TypeConverter
    fun stringToSite(value: String?): Site? =
        value?.let { Site.values().firstOrNull { it.value == value } }

    @TypeConverter
    fun siteToString(value: Site?): String? = value?.value
}