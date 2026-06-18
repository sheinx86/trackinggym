package com.example.trackinggym.data.entities

import androidx.annotation.Keep

@Keep
data class BackupData(
    val exercises: List<Exercise>,
    val logs: List<ExerciseLog>
)
