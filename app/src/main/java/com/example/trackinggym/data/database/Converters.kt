package com.example.trackinggym.data.database

import androidx.room.TypeConverter
import com.example.trackinggym.data.entities.SetRecord
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromSetRecordList(value: List<SetRecord>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toSetRecordList(value: String): List<SetRecord> {
        val type = object : TypeToken<List<SetRecord>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromLongList(value: List<Long>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toLongList(value: String): List<Long> {
        val type = object : TypeToken<List<Long>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
}
