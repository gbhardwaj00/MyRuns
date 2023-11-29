package com.stressmeter.myruns

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExerciseEntryRepository(private val exerciseEntryDatabaseDAO: ExerciseEntryDatabaseDAO) {

    val allExerciseEntries : Flow<List<ExerciseEntry>> = exerciseEntryDatabaseDAO.getAllExerciseEntries()

    fun insert(exerciseEntry: ExerciseEntry) {
        CoroutineScope(IO).launch {
            exerciseEntryDatabaseDAO.insertExerciseEntry(exerciseEntry)
        }
    }

    fun delete(key: Long) {
        CoroutineScope(IO).launch {
            exerciseEntryDatabaseDAO.deleteExerciseEntry(key)
        }
    }

    suspend fun getEntry(key: Long): ExerciseEntry {
        return withContext(IO) {
            exerciseEntryDatabaseDAO.getEntry(key)
        }
    }
}