package com.example.trackinggym.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackinggym.data.database.AppDatabase
import com.example.trackinggym.data.entities.Exercise
import com.example.trackinggym.data.entities.ExerciseLog
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.exerciseDao()

    val exercises = dao.getAllExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteExercises = dao.getFavoriteExercisesWithLatestLog()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addExercise(name: String, category: String, isFavorite: Boolean) {
        viewModelScope.launch {
            dao.insertExercise(Exercise(name = name, category = category, isFavorite = isFavorite))
        }
    }

    fun toggleFavorite(exercise: Exercise) {
        viewModelScope.launch {
            dao.updateExercise(exercise.copy(isFavorite = !exercise.isFavorite))
        }
    }

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            dao.deleteExercise(exercise)
        }
    }

    fun deleteExerciseLog(log: ExerciseLog) {
        viewModelScope.launch {
            dao.deleteExerciseLog(log)
        }
    }

    fun saveWorkout(exerciseId: Long, dateMs: Long, sets: List<com.example.trackinggym.data.entities.SetRecord>, comments: String) {
        viewModelScope.launch {
            dao.insertExerciseLog(
                ExerciseLog(
                    exerciseId = exerciseId,
                    dateMs = dateMs,
                    sets = sets,
                    comments = comments
                )
            )
        }
    }

    fun getLogsForExercise(exerciseId: Long) = dao.getLogsForExercise(exerciseId)

    suspend fun getExportDataJson(): String {
        val exercises = dao.getAllExercisesList()
        val logs = dao.getAllLogsList()
        val backup = com.example.trackinggym.data.entities.BackupData(exercises, logs)
        return com.google.gson.Gson().toJson(backup)
    }

    suspend fun importDataFromJson(json: String): Boolean {
        return try {
            val backup = com.google.gson.Gson().fromJson(json, com.example.trackinggym.data.entities.BackupData::class.java)
            if (backup != null) {
                // To replace safely we should ideally use a transaction, but doing sequentially is fine
                // First delete
                dao.clearAllLogs()
                dao.clearAllExercises()
                // Then insert
                dao.insertAllExercises(backup.exercises)
                dao.insertAllLogs(backup.logs)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
