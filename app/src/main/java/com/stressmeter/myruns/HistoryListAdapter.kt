package com.stressmeter.myruns

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider

class HistoryListAdapter(private val context: Context, private var exercisesList : List<ExerciseEntry>) : BaseAdapter() {

    override fun getCount(): Int {
        return exercisesList.size
    }

    override fun getItem(position: Int): Any {
        return exercisesList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        Log.d("HistoryFragment", "getView: $position")
        val view: View = View.inflate(context, R.layout.exercise_entry_layout,null)

        val title = view.findViewById(R.id.entry_title) as TextView
        val detail = view.findViewById(R.id.entry_detail) as TextView

        val sharedPreferences = context.getSharedPreferences(
            context.getString(R.string.settings_preference_key),
            Context.MODE_PRIVATE
        )

        var units = sharedPreferences.getString("key_units_pref", "kms")
        if (units == "array/unit_values/0") {
            units = "kms"
        }
        val distMultiplier = if (units == "miles") {
            0.621371
        } else {
            1.0
        }
        val exerciseEntry = exercisesList[position]
        val duration = exerciseEntry.duration
        val minutes = duration.toInt()
        val seconds = ((duration - minutes) * 60).toInt()
        title.text = "${exerciseEntry.inputType}: ${exerciseEntry.activityType}, ${exerciseEntry.dateTime}"
        detail.text = "${minutes} mins ${seconds} secs, ${"%.2f".format(exerciseEntry.distance * distMultiplier)} $units"


        val intent : Intent = if(exerciseEntry.inputType == "Manual Entry") {
            Intent(context, ExerciseEntryDisplayActivity::class.java)
        } else {
            Intent(context, MapActivity::class.java)
        }
        intent.putExtra("exerciseEntryId", exerciseEntry.id)
        intent.putExtra("unitType", units)

        view.setOnClickListener {
            context.startActivity(intent)
        }
        return view
    }

    fun replace(newExercisesList: List<ExerciseEntry>){
        exercisesList = newExercisesList
    }
}
