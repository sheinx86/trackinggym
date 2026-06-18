package com.example.trackinggym.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.trackinggym.data.entities.Exercise
import com.example.trackinggym.data.entities.ExerciseLog
import com.example.trackinggym.data.entities.ExerciseWithLatestLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises")
    fun getAllExercises(): Flow<List<Exercise>>

    @Query("""
        SELECT e.*, 
               l.dateMs AS latestLogDateMs, 
               l.sets AS latestLogSets
        FROM exercises e
        LEFT JOIN (
            SELECT exerciseId, MAX(dateMs) as maxDate
            FROM exercise_logs
            GROUP BY exerciseId
        ) latest ON e.id = latest.exerciseId
        LEFT JOIN exercise_logs l ON l.exerciseId = latest.exerciseId AND l.dateMs = latest.maxDate
        WHERE e.isFavorite = 1
    """)
    fun getFavoriteExercisesWithLatestLog(): Flow<List<ExerciseWithLatestLog>>

    @Query("SELECT * FROM exercise_logs WHERE exerciseId = :exerciseId ORDER BY dateMs DESC")
    fun getLogsForExercise(exerciseId: Long): Flow<List<ExerciseLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise): Long

    @Update
    suspend fun updateExercise(exercise: Exercise)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseLog(log: ExerciseLog): Long

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Delete
    suspend fun deleteExerciseLog(log: ExerciseLog)

    // Export & Import support
    @Query("SELECT * FROM exercises")
    suspend fun getAllExercisesList(): List<Exercise>

    @Query("SELECT * FROM exercise_logs")
    suspend fun getAllLogsList(): List<ExerciseLog>

    @Query("DELETE FROM exercises")
    suspend fun clearAllExercises()

    @Query("DELETE FROM exercise_logs")
    suspend fun clearAllLogs()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllExercises(exercises: List<Exercise>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLogs(logs: List<ExerciseLog>)
}
