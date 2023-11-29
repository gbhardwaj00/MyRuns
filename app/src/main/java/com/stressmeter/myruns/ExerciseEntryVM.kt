package com.stressmeter.myruns

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData

class ExerciseEntryVM(private val repository: ExerciseEntryRepository) : ViewModel() {
    val allExercisesLiveData : LiveData<List<ExerciseEntry>> = repository.allExerciseEntries.asLiveData()

    fun insert(exerciseEntry: ExerciseEntry) {
        repository.insert(exerciseEntry)
    }

    fun delete(key: Long) {
        repository.delete(key)
    }

    suspend fun getEntry(key: Long): ExerciseEntry {
        return repository.getEntry(key)
    }
}

class ExerciseEntryVMFactory(private val repository: ExerciseEntryRepository) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseEntryVM::class.java))
            return ExerciseEntryVM(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}