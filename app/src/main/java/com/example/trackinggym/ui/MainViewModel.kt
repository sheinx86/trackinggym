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

    fun addExercise(name: String, isFavorite: Boolean) {
        viewModelScope.launch {
            dao.insertExercise(Exercise(name = name, isFavorite = isFavorite))
        }
    }

    fun toggleFavorite(exercise: Exercise) {
        viewModelScope.launch {
            dao.updateExercise(exercise.copy(isFavorite = !exercise.isFavorite))
        }
    }

    fun saveWorkout(exerciseId: Long, dateMs: Long, sets: List<com.example.trackinggym.data.entities.SetRecord>) {
        viewModelScope.launch {
            dao.insertExerciseLog(
                ExerciseLog(
                    exerciseId = exerciseId,
                    dateMs = dateMs,
                    sets = sets
                )
            )
        }
    }

    fun getLogsForExercise(exerciseId: Long) = dao.getLogsForExercise(exerciseId)
}
