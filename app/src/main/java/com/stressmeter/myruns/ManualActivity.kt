package com.stressmeter.myruns

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import java.util.Calendar


class ManualActivity : AppCompatActivity(){
    private lateinit var database: ExerciseEntryDatabase
    private lateinit var databaseDao: ExerciseEntryDatabaseDAO
    private lateinit var repository: ExerciseEntryRepository
    private lateinit var viewModelFactory: ExerciseEntryVMFactory
    private lateinit var exerciseEntryVM: ExerciseEntryVM

    private lateinit var lvManualActivity: ListView
    private lateinit var listAdapter: ArrayAdapter<String>
    private lateinit var manualActivityVM : ManualActivityVM

    private lateinit var saveButton : Button
    private lateinit var cancelButton : Button

    private var date = Calendar.getInstance().get(Calendar.DAY_OF_MONTH).toString() + "/" +
            (Calendar.getInstance().get(Calendar.MONTH)+1).toString() + "/" +
        Calendar.getInstance().get(Calendar.YEAR).toString()
    private var time = Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toString() + ":" +
        Calendar.getInstance().get(Calendar.MINUTE).toString()

    private var duration : Double = 0.0
    private var distance : Double = 0.0
    private var calories : Double = 0.0
    private var heartRate : Double = 0.0
    private var comment : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual)

        // Initialize UI elements
        saveButton = findViewById(R.id.saveManualEntryBtn)
        cancelButton = findViewById(R.id.cancelManualEntryBtn)

        lvManualActivity = findViewById(R.id.lvManualAct)
        listAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.manual_activity_options))
        lvManualActivity.adapter = listAdapter

        // Initialize ViewModel
        manualActivityVM = ViewModelProvider(this).get(ManualActivityVM::class.java)
        manualActivityVM.date.observe(this) {
            date = it
            Log.d("XD", "date: $date")
        }
        manualActivityVM.time.observe(this) {
            time = it
            Log.d("XD", "time: $time")
        }
        manualActivityVM.duration.observe(this) {
            duration = it
            Log.d("XD", "duration: $duration")
        }
        manualActivityVM.distance.observe(this) {
            distance = it
            Log.d("XD", "distance: $distance")
        }
        manualActivityVM.calories.observe(this) {
            calories = it
            Log.d("XD", "calories: $calories")
        }
        manualActivityVM.heartRate.observe(this) {
            heartRate = it
            Log.d("XD", "heartRate: $heartRate")
        }
        manualActivityVM.comment.observe(this) {
            comment = it
            Log.d("XD", "comment: $comment")
        }

        // Set onClickListeners on each list item
        lvManualActivity.setOnItemClickListener { _, _, position, _ ->
            val myDialog = MyRunsDialogFragment()
            val bundle = Bundle()
            when (position) {
                0 -> {
                    bundle.putInt(
                        MyRunsDialogFragment.DIALOG_KEY,
                        MyRunsDialogFragment.datepickerDialog
                    )
                    myDialog.arguments = bundle
                    myDialog.show(supportFragmentManager, "my dialog")
                }

                1 -> {
                    bundle.putInt(
                        MyRunsDialogFragment.DIALOG_KEY,
                        MyRunsDialogFragment.timepickerDialog
                    )
                    myDialog.arguments = bundle
                    myDialog.show(supportFragmentManager, "my dialog")
                }

                2 -> {
                    bundle.putInt(
                        MyRunsDialogFragment.DIALOG_KEY,
                        MyRunsDialogFragment.durationDialog
                    )
                    myDialog.arguments = bundle
                    myDialog.show(supportFragmentManager, "my dialog")
                }

                3 -> {
                    bundle.putInt(
                        MyRunsDialogFragment.DIALOG_KEY,
                        MyRunsDialogFragment.distanceDialog
                    )
                    myDialog.arguments = bundle
                    myDialog.show(supportFragmentManager, "my dialog")
                }

                4 -> {
                    bundle.putInt(
                        MyRunsDialogFragment.DIALOG_KEY,
                        MyRunsDialogFragment.caloriesDialog
                    )
                    myDialog.arguments = bundle
                    myDialog.show(supportFragmentManager, "my dialog")
                }

                5 -> {
                    bundle.putInt(
                        MyRunsDialogFragment.DIALOG_KEY,
                        MyRunsDialogFragment.heartRateDialog
                    )
                    myDialog.arguments = bundle
                    myDialog.show(supportFragmentManager, "my dialog")
                }

                6 -> {
                    bundle.putInt(
                        MyRunsDialogFragment.DIALOG_KEY,
                        MyRunsDialogFragment.commentDialog
                    )
                    myDialog.arguments = bundle
                    myDialog.show(supportFragmentManager, "my dialog")
                }
            }
        }

        // Initialize database to add entry
        database = ExerciseEntryDatabase.getInstance(this)
        databaseDao = database.exerciseEntryDatabaseDAO
        repository = ExerciseEntryRepository(databaseDao)
        viewModelFactory = ExerciseEntryVMFactory(repository)
        exerciseEntryVM = viewModelFactory.create(ExerciseEntryVM::class.java)

        // Set onClickListeners on buttons
        saveButton.setOnClickListener {
            val entry = ExerciseEntry()
            val activityTypeSpinnerEntries = resources.getStringArray(R.array.activityTypeSpinnerEntries)
            val inputTypeSpinnerEntries = resources.getStringArray(R.array.inputTypeSpinnerEntries)
            entry.inputType = inputTypeSpinnerEntries[intent.extras!!.getInt("inputType")]
            entry.activityType = activityTypeSpinnerEntries[intent.extras!!.getInt("activityType")]
            entry.dateTime = "$date $time"
            entry.duration = duration
            entry.distance = distance
            entry.calorie = calories
            entry.heartRate = heartRate
            entry.comment = comment
            entry.avgSpeed = if (duration == 0.0) 0.0 else distance/duration
            entry.avgPace = if (distance == 0.0) 0.0 else duration/distance
            entry.climb = 0.0
            entry.locationList.add(LatLng(0.0, 0.0))

            exerciseEntryVM.insert(entry)
            Toast.makeText(this, "Entry saved", Toast.LENGTH_SHORT).show()
            finish()
        }

        cancelButton.setOnClickListener {
            Toast.makeText(this, "Entry discarded", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
