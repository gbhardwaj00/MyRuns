package com.stressmeter.myruns

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ExerciseEntryDisplayActivity : AppCompatActivity(){
    private lateinit var database: ExerciseEntryDatabase
    private lateinit var databaseDao: ExerciseEntryDatabaseDAO
    private lateinit var repository: ExerciseEntryRepository
    private lateinit var viewModelFactory: ExerciseEntryVMFactory
    private lateinit var myViewModel: ExerciseEntryVM

    private lateinit var deleteButton : Button
    private lateinit var inputType : TextView
    private lateinit var activityType : TextView
    private lateinit var dateTime : TextView
    private lateinit var duration : TextView
    private lateinit var distance : TextView
    private lateinit var avgPace : TextView
    private lateinit var avgSpeed : TextView
    private lateinit var calorie : TextView
    private lateinit var heartRate : TextView
    private lateinit var comment : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.exercise_entry_display)

        deleteButton = findViewById(R.id.deleteEntrybtn)
        inputType = findViewById(R.id.tvInputType)
        activityType = findViewById(R.id.tvActivityType)
        dateTime = findViewById(R.id.tvDateAndTime)
        duration = findViewById(R.id.tvDuration)
        distance = findViewById(R.id.tvDistance)
        avgPace = findViewById(R.id.tvAvgPace)
        avgSpeed = findViewById(R.id.tvAvgSpeed)
        calorie = findViewById(R.id.tvCalorie)
        heartRate = findViewById(R.id.tvHeartRate)
        comment = findViewById(R.id.tvComment)

        database = ExerciseEntryDatabase.getInstance(this)
        databaseDao = database.exerciseEntryDatabaseDAO
        repository = ExerciseEntryRepository(databaseDao)
        viewModelFactory = ExerciseEntryVMFactory(repository)
        myViewModel = viewModelFactory.create(ExerciseEntryVM::class.java)

        val unitType = intent.getStringExtra("unitType")
        val exerciseEntryId = intent.getLongExtra("exerciseEntryId", 0)
        Log.d("ExerciseEntryDisplay", "entryId: $exerciseEntryId")

        lifecycleScope.launch {
            val entry = myViewModel.getEntry(exerciseEntryId)
            Log.d("ExerciseEntryDisplay", "entry: $entry")

            inputType.text = entry.inputType
            activityType.text = entry.activityType
            dateTime.text = entry.dateTime
            duration.text = entry.duration.toString() + " mins"
            distance.text = if (unitType == "miles") {
                "%.2f miles".format(entry.distance/1.60934)
            } else {
                "%.2f kms".format(entry.distance)
            }
            avgPace.text = if (unitType == "miles") {
                "%.2f min/mile".format(entry.avgPace/1.60934)
            } else {
                "%.2f min/km".format(entry.avgPace)
            }
            avgSpeed.text = if (unitType == "miles") {
                "%.2f miles/hr".format(entry.avgSpeed/1.60934)
            } else {
                "%.2f kms/hr".format(entry.avgSpeed)
            }
            calorie.text = entry.calorie.toString()
            heartRate.text = entry.heartRate.toString()
            comment.text = entry.comment
        }

        deleteButton.setOnClickListener {
            myViewModel.delete(exerciseEntryId)
            finish()
        }
    }
}
