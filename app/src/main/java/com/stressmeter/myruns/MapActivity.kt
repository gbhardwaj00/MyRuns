package com.stressmeter.myruns

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.launch
import java.util.Calendar


class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private val PERMISSION_REQUEST_CODE = 0

    // UI
    private lateinit var saveBtn : Button
    private lateinit var cancelBtn : Button

    private lateinit var tvactivityType : TextView
    private lateinit var tvavgSpeed : TextView
    private lateinit var tvcurSpeed : TextView
    private lateinit var tvclimb : TextView
    private lateinit var tvcalorie : TextView
    private lateinit var tvdistance : TextView
    private lateinit var deleteBtn : Button
    private lateinit var exerciseEntryVM: ExerciseEntryVM

    // service
    private lateinit var appContext: Context
    private lateinit var mapViewModel: MapViewModel
    private var isBound = false
    private val BIND_STATUS_KEY = "bind_status_key"
    private lateinit var backPressedCallback : OnBackPressedCallback
    private lateinit var serviceIntent: Intent

    // map
    private lateinit var map: GoogleMap
    private var mapCentered = false
    private lateinit var  markerOptions: MarkerOptions
    private lateinit var  polylineOptions: PolylineOptions
    private lateinit var  polylines: ArrayList<Polyline>
    private var locationList = ArrayList<LatLng>()
    private var currentLocationMarker : Marker? = null
    private var firstMarkerSet = false

    // database
    private lateinit var inputType : String
    private lateinit var activityType : String

    private var date = Calendar.getInstance().get(Calendar.DAY_OF_MONTH).toString() + "/" +
            (Calendar.getInstance().get(Calendar.MONTH)+1).toString() + "/" +
            Calendar.getInstance().get(Calendar.YEAR).toString()
    private var time = Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toString() + ":" +
            Calendar.getInstance().get(Calendar.MINUTE).toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map_activity)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this) ?: Log.e("MapActivity", "Map Fragment is null")

        val activityTypeSpinnerEntries = resources.getStringArray(R.array.activityTypeSpinnerEntries)
        val inputTypeSpinnerEntries = resources.getStringArray(R.array.inputTypeSpinnerEntries)

        tvactivityType = findViewById(R.id.statActType)
        activityType = activityTypeSpinnerEntries[intent.extras!!.getInt("activityType")]
        tvactivityType.text = "Activity: ${activityType}"
        inputType = inputTypeSpinnerEntries[intent.extras!!.getInt("inputType")]
        tvavgSpeed = findViewById(R.id.statAvgSpeed)
        tvcurSpeed = findViewById(R.id.statCurSpeed)
        tvclimb = findViewById(R.id.statClimb)
        tvcalorie = findViewById(R.id.statCalories)
        tvdistance = findViewById(R.id.statDistance)
        saveBtn = findViewById(R.id.saveMapActbtn)
        cancelBtn = findViewById(R.id.CancelMapActbtn)
        deleteBtn = findViewById(R.id.deleteMapEntrybtn)

        serviceIntent = Intent(this, TrackingService::class.java)
        appContext = applicationContext

        val database = ExerciseEntryDatabase.getInstance(this)
        val databaseDao = database.exerciseEntryDatabaseDAO
        val repository = ExerciseEntryRepository(databaseDao)
        val viewModelFactory = ExerciseEntryVMFactory(repository)
        exerciseEntryVM = viewModelFactory.create(ExerciseEntryVM::class.java)

        val extras = intent.extras
        if(extras != null){
            if (extras.containsKey("exerciseEntryId")){
                saveBtn.visibility = View.GONE
                cancelBtn.visibility = View.GONE
            }
            else {
                deleteBtn.visibility = View.GONE
            }
        }

        mapViewModel = ViewModelProvider(this).get(MapViewModel::class.java)

        if(savedInstanceState != null)
            isBound = savedInstanceState.getBoolean(BIND_STATUS_KEY)

        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                stopService(serviceIntent)
                isEnabled = false
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        val sharedPref = getSharedPreferences(getString(R.string.settings_preference_key), MODE_PRIVATE)
        val units = sharedPref.getString("key_units_pref", "kms")

        cancelBtn.setOnClickListener {
            finish()
        }

        saveBtn.setOnClickListener {
            stopService(serviceIntent)
            isBound = false
            saveExerEntry()
            finish()
        }
        checkLocationPermission()
    }

    private fun saveExerEntry() {
        val entry = ExerciseEntry()
        entry.dateTime = "$date $time"
        entry.inputType = inputType
        entry.activityType = activityType
        entry.duration = mapViewModel.duration.value!!
        entry.distance = mapViewModel.distance.value!!
        entry.avgSpeed = mapViewModel.avgSpeed.value!!
        entry.avgPace = mapViewModel.avgPace.value!!
        entry.climb = mapViewModel.climb.value!!
        entry.calorie = mapViewModel.calorie.value!!
        entry.heartRate = 0.0
        entry.comment = ""
        entry.locationList = locationList
        exerciseEntryVM.insert(entry)
        Toast.makeText(this, "Entry saved", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun checkLocationPermission() {
        if (Build.VERSION.SDK_INT < 23) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
        else
            bindService()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                bindService()
        }
    }

    override fun finish() {
        super.finish()
        if(isBound) {
            appContext.unbindService(mapViewModel)
            appContext.stopService(serviceIntent)
            isBound = false
        }
    }

    private fun bindService() {
        if (!isBound) {
            appContext.startService(serviceIntent)
            appContext.bindService(serviceIntent, mapViewModel, Context.BIND_AUTO_CREATE)
            isBound = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        backPressedCallback.remove()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(BIND_STATUS_KEY, isBound)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        polylineOptions = PolylineOptions()
        polylineOptions.color(Color.BLACK)
        polylines = ArrayList()
        markerOptions = MarkerOptions()

        checkLocationPermission()

        val extras = intent.extras
        if (extras != null) {
            val unitType = extras?.getString("unitType")
            if (intent.extras!!.containsKey("exerciseEntryId")) {
                val exerciseEntryId = extras?.getLong("exerciseEntryId")
                lifecycleScope.launch {
                    val entry = exerciseEntryId?.let { exerciseEntryVM.getEntry(it) }
                    if (entry != null) {
                        tvactivityType.text = "Activity type: ${entry.activityType}"
                        tvdistance.text = if (unitType == "miles") {
                            "Distance: %.2f miles".format(entry.distance/1.60934)
                        } else {
                            "Distance: %.2f kms".format(entry.distance)
                        }
                        tvavgSpeed.text = if (unitType == "miles") {
                            "Average Speed: %.2f miles/hr".format(entry.avgSpeed/1.60934)
                        } else {
                            "Average Speed: %.2f kms/hr".format(entry.avgSpeed)
                        }
                        tvcurSpeed.text = if (unitType == "miles") {
                            "Current Speed: 0.0 miles/hr".format(entry.avgSpeed/1.60934)
                        } else {
                            "Current Speed: 0.0 kms/hr".format(entry.avgSpeed)
                        }
                        tvclimb.text = "Climb: 0 m"
                        tvcalorie.text = "Calories: 0".format(entry.calorie)
                        updateMap(entry.locationList)
                    }
                    deleteBtn.setOnClickListener {
                        if (exerciseEntryId != null) {
                            exerciseEntryVM.delete(exerciseEntryId)
                        }
                        finish()
                    }
                }
            }
            else {
                tvactivityType.text = "Activity type: ${activityType}"
                tvdistance.text = if (unitType == "miles") {
                    "Distance: 0.0 miles"
                } else {
                    "Distance: 0.0 kms"
                }
                tvavgSpeed.text = if (unitType == "miles") {
                    "Average Speed: 0.0 miles/hr"
                } else {
                    "Average Speed: 0.0 kms/hr"
                }
                tvclimb.text = "Climb: 0.0 m"
                tvcalorie.text = "Calories: 0.0 kcals"
                tvcurSpeed.text = "Current Speed: 0.0 "
                if(inputType == "Automatic") {
                    Log.d("gb", "inputType Automatic")
                    tvactivityType.text = "Activity type: Standing"
                    mapViewModel.wekaActivity.observe(this) {
                        Log.d("gb", "wekaActivity.observe")
                        activityType = it
                        tvactivityType.text = "Activity type: ${activityType}"
                    }
                }
                mapViewModel.locationList.observe(this) {
//                    Log.d("gb", "locationList.observe")
                    locationList = it
                    mapCentered = false
                    updateMap(locationList)
                }
            }
        }
    }

    private fun updateMap(locationList: ArrayList<LatLng>) {
        if (locationList.isEmpty()) {
//            Log.d("gb", "updateMap locationlistEmpty")
            return
        }
        val currentLocation = locationList.last()
        if (!firstMarkerSet) {
            setFirstMarker(locationList.first())
            firstMarkerSet = true
        }
        if (locationList.size >= 2) {
            if (intent.extras != null) {
                if (intent.extras!!.containsKey("exerciseEntryId")) {
                    val exerciseEntryId = intent.extras!!.getLong("exerciseEntryId")
                    lifecycleScope.launch {
                        val entry = exerciseEntryVM.getEntry(exerciseEntryId)
                        polylineOptions.addAll(locationList)
                        polylines.add(map.addPolyline(polylineOptions))
                    }
                }
                else {
                    polylineOptions.add(locationList[locationList.size - 2], currentLocation)
                    polylines.add(map.addPolyline(polylineOptions))
                }
            }
        }
        if (!mapCentered) {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLocation, 17f)
            map.animateCamera(cameraUpdate)
            mapCentered = true
        }
        updateLastLocationMarker(currentLocation)
        if (intent.extras != null) {
            if (intent.extras!!.containsKey("exerciseEntryId")) {
                return
            }
            else {
                updateStats()
            }
        }
    }

    private fun setFirstMarker(first: LatLng) {
        markerOptions.position(first)
        map.addMarker(markerOptions)
    }

    private fun updateStats() {
        val distance = mapViewModel.distance.value // convert to kms
        val avgSpeed = mapViewModel.avgSpeed.value
        val curSpeed = mapViewModel.curSpeed.value
        val climb = mapViewModel.climb.value
        val calorie = mapViewModel.calorie.value

        tvavgSpeed.text = String.format("Avg. Speed: %.2f km/h", avgSpeed)
        tvcurSpeed.text = String.format("Cur. Speed: %.2f km/h", curSpeed)
        tvclimb.text = String.format("Climb: %.2f m", climb)
        tvcalorie.text = String.format("Calories: %.2f kcals", calorie)
        tvdistance.text = String.format("Distance: %.2f kms", distance)
    }

    private fun updateLastLocationMarker(currentLocation: LatLng) {
        if (currentLocationMarker != null) {
            currentLocationMarker!!.remove()
        }
        markerOptions.position(currentLocation)
        currentLocationMarker = map.addMarker(markerOptions)
    }
}