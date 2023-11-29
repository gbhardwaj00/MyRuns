package com.stressmeter.myruns

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import java.util.ArrayList

class FragmentHistory : Fragment()  {
    private lateinit var lvExerciseEntries : ListView

    private lateinit var arrayList: ArrayList<ExerciseEntry>
    private lateinit var historyListAdapter: HistoryListAdapter

    private lateinit var database: ExerciseEntryDatabase
    private lateinit var databaseDao: ExerciseEntryDatabaseDAO
    private lateinit var repository: ExerciseEntryRepository
    private lateinit var viewModelFactory: ExerciseEntryVMFactory
    private lateinit var myViewModel: ExerciseEntryVM

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        Log.d("xd", "onCreateView: ")

        lvExerciseEntries = view.findViewById(R.id.lvExerciseEntries)
        arrayList = ArrayList()
        historyListAdapter = HistoryListAdapter(requireActivity(), arrayList)
        lvExerciseEntries.adapter = historyListAdapter

        database = ExerciseEntryDatabase.getInstance(requireActivity())
        databaseDao = database.exerciseEntryDatabaseDAO
        repository = ExerciseEntryRepository(databaseDao)
        viewModelFactory = ExerciseEntryVMFactory(repository)
        myViewModel = viewModelFactory.create(ExerciseEntryVM::class.java)
        return view
    }

    override fun onResume() {
        super.onResume()
        Log.d("xd", "onResume: ")
        myViewModel.allExercisesLiveData.observe(requireActivity(), androidx.lifecycle.Observer {
            historyListAdapter.replace(it)
            historyListAdapter.notifyDataSetChanged()
            Log.d("HistoryFragment", "onResume: ${it.size}")
        })
    }
}
