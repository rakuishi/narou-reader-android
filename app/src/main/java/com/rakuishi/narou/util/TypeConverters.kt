package com.rakuishi.narou.util

import androidx.room.TypeConverter
import java.util.*

class DateConverter {

    @TypeConverter
    fun longToDate(value: Long?): Date? = value?.let { Date(value) }

    @TypeConverter
    fun dateToLong(date: Date?): Long? = date?.time
}