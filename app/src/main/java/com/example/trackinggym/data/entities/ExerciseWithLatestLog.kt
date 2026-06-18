package com.example.trackinggym.data.entities

import androidx.room.Embedded

data class ExerciseWithLatestLog(
    @Embedded val exercise: Exercise,
    val latestLogDateMs: Long?,
    val latestLogSets: List<SetRecord>?
)
