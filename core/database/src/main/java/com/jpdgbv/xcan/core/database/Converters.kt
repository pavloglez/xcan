package com.jpdgbv.xcan.core.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringMap(map: Map<String, Float>?): String? {
        if (map == null) return null
        return gson.toJson(map)
    }

    @TypeConverter
    fun toStringMap(data: String?): Map<String, Float>? {
        if (data == null) return null
        val type = object : TypeToken<Map<String, Float>>() {}.type
        return try {
            gson.fromJson(data, type)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
