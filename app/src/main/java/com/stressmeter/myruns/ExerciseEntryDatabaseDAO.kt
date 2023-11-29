package com.stressmeter.myruns

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseEntryDatabaseDAO {

    @Insert
    suspend fun insertExerciseEntry(exerciseEntry: ExerciseEntry)

    @Query("SELECT * FROM exercise_table")
    fun getAllExerciseEntries(): Flow<List<ExerciseEntry>>

    @Query("DELETE FROM exercise_table WHERE id = :key")
    suspend fun deleteExerciseEntry(key: Long)

    @Query("SELECT * FROM exercise_table WHERE id = :key")
    suspend fun getEntry(key: Long): ExerciseEntry
}