package com.example.trackinggym.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val isFavorite: Boolean,
    val category: String = "Pecho"
)
