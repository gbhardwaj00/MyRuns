package com.stressmeter.myruns

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.icu.text.SimpleDateFormat
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.ArrayBlockingQueue
import kotlin.math.sqrt

class TrackingService : Service(), LocationListener, SensorEventListener {
    // notification and message handling
    private lateinit var notificationManager : NotificationManager
    private val NOTIFICATION_ID = 123
    private val CHANNEL_ID = "notification channel"
    private lateinit var myBinder: MyBinder
    private var msgHandler: Handler? = null

    // sensor
    private lateinit var sensorManager : SensorManager
    private lateinit var accelerometer : Sensor
    private var x: Double = 0.0
    private var y: Double = 0.0
    private var z: Double = 0.0
    private var mAccBuffer: ArrayBlockingQueue<Double> = ArrayBlockingQueue(Globals.ACCELEROMETER_BUFFER_CAPACITY)
    var OnSensorChangedTask: Job? = null

    //database
    private var avgSpeed : Double = 0.0
    private var avgPace : Double = 0.0
    private var climb : Double = 0.0
    private var curSpeed : Float = 0f
    private var distance : Double = 0.0
    private var calorie : Double = 0.0
    private var duration : Double = 0.0

    //location
    private lateinit var locationManager: LocationManager
    private lateinit var currentLocation : LatLng
    private var startingTime : Long = 0
    private lateinit var lastLocation : Location
    private var lastTime : Long = 0
    private var firstTime : Boolean = true

    companion object{
        const val MSG_INT_VALUE = 0
        const val WEKA_MSG_INT_VALUE = 1
        const val WEKA_MSG_INT_KEY = "weka_msg_int_key"
    }

    override fun onCreate() {
        super.onCreate()
        myBinder = MyBinder()
        currentLocation = LatLng(0.0, 0.0)
        startingTime = System.currentTimeMillis()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        initLocationManager()
        //inspired from "https://github.com/annapoliswu/MyRuns/blob/master/app/src/main/java/com/zw/myruns/TrackingService.kt"
        OnSensorChangedTask = CoroutineScope(IO).launch{
            OnSensorChangedTask()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        showNotification()
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        return myBinder
    }

    inner class MyBinder : Binder() {
        fun setmsgHandler(msgHandler: Handler) {
            this@TrackingService.msgHandler = msgHandler
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        cleanupTasks()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanupTasks()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        cleanupTasks()
        stopSelf()
    }

    private fun cleanupTasks() {
//        Log.d("db", "Tracking Service Cleanup Tasks")
        avgSpeed = 0.0
        avgPace = 0.0
        climb = 0.0
        curSpeed = 0f
        distance = 0.0
        calorie = 0.0
        duration = 0.0

        msgHandler = null
        notificationManager.cancel(NOTIFICATION_ID)
        if(locationManager != null)
            locationManager.removeUpdates(this)
        currentLocation = LatLng(0.0, 0.0)
        startingTime = 0
        lastLocation = Location("")
        lastTime = 0
        firstTime = true
        sensorManager.unregisterListener(this)
        OnSensorChangedTask?.cancel()
    }


    private fun initLocationManager() {
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) return

            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null) {
//                Log.d("gb", "initLocationManager ${location.latitude} ${location.longitude}")
                onLocationChanged(location)
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1f, this)

        } catch (e: SecurityException) {
        }
    }

    override fun onLocationChanged(location: Location) {
//        Log.d("gb", "Tracking Service OnLocChanged${location.latitude} ${location.longitude}")
        val lat = location.latitude
        val lng = location.longitude
        val latLng = LatLng(lat, lng)
        currentLocation = latLng
        if(firstTime){
//            Log.d("gb", "Tracking Service First Time ${location.latitude} ${location.longitude}")
            firstTime = false
        }
        if (::lastLocation.isInitialized) {
            if(location.distanceTo(lastLocation) < 1000) {
                distance += location.distanceTo(lastLocation) / 1000 // kms
                climb += if (location.altitude > lastLocation.altitude) location.altitude - lastLocation.altitude else 0.0 // meter
                duration = (System.currentTimeMillis() - startingTime) / (1000.0 * 60) // min
                calorie = distance * 0.6
                avgSpeed = (distance) / (duration / 60) // km/hr
                avgPace = (duration) / distance // min/km
                curSpeed =
                    (location.distanceTo(lastLocation) / 1000f) / ((System.currentTimeMillis() - lastTime) / (1000f * 60 * 60)) // km/hr
//                Log.d("speed", "curSpeed: $curSpeed")
            }
        }
        lastLocation = location
        lastTime = System.currentTimeMillis()
        sendDetails()
    }

    private fun showNotification() {
        val intent = Intent(this, MapActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
        notificationBuilder.setSmallIcon(com.google.android.material.R.drawable.mtrl_checkbox_button_icon_checked_unchecked)
        notificationBuilder.setContentTitle("Your location is being tracked")
        notificationBuilder.setContentText("Tap me to go back")
        notificationBuilder.setContentIntent(pendingIntent)
        val notification = notificationBuilder.build()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "channel name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun sendDetails(){
        try {
            val bundle = Bundle()
            bundle.putDouble("AVG_SPEED_KEY", avgSpeed)
            bundle.putDouble("AVG_PACE_KEY", avgPace)
            bundle.putDouble("CLIMB_KEY", climb)
            bundle.putFloat("CUR_SPEED_KEY", curSpeed)
            bundle.putDouble("DISTANCE_KEY", distance)
            bundle.putDouble("CALORIE_KEY", calorie)
            bundle.putDouble("DURATION_KEY", duration)
//            Log.d("gb", "Tracking Service Send Details ${currentLocation.latitude} ${currentLocation.longitude}")
            bundle.putDouble("LAT_KEY", currentLocation.latitude)
            bundle.putDouble("LNG_KEY", currentLocation.longitude)

            val msg = Message.obtain()
            msg.what = MSG_INT_VALUE
            msg.data = bundle
            msgHandler?.sendMessage(msg)
        } catch (t: Throwable) {
            println("debug: Timer Tick Failed. $t")
        }
    }

    // This code is inspired from a github repo and modified to fit our needs
    // Code is also inspired from 'MyRunDataCollectorKotlin' project provided by XD in Week 11, Lecture 15
    // "https://github.com/annapoliswu/MyRuns/blob/master/app/src/main/java/com/zw/myruns/Globals.kt"
    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            x = (event.values[0] / SensorManager.GRAVITY_EARTH).toDouble()
            y = (event.values[1] / SensorManager.GRAVITY_EARTH).toDouble()
            z = (event.values[2] / SensorManager.GRAVITY_EARTH).toDouble()
        }
        val m = sqrt(x * x + y * y + z* z)
        try {
            mAccBuffer.add(m)
        } catch (e: IllegalStateException) {

            // Exception happens when reach the capacity.
            // Doubling the buffer. ListBlockingQueue has no such issue,
            // But generally has worse performance
            val newBuf = ArrayBlockingQueue<Double>(mAccBuffer.size * 2)
            mAccBuffer.drainTo(newBuf)
            mAccBuffer = newBuf
            mAccBuffer.add(m)
        }
    }

    private suspend fun OnSensorChangedTask() {
            val featureVector = ArrayList<Double>(Globals.ACCELEROMETER_BLOCK_CAPACITY)
            var blockSize = 0
            val fft = FFT(Globals.ACCELEROMETER_BLOCK_CAPACITY)
            val accBlock = DoubleArray(Globals.ACCELEROMETER_BLOCK_CAPACITY)
            val im = DoubleArray(Globals.ACCELEROMETER_BLOCK_CAPACITY)
            var max = Double.MIN_VALUE
            while (true) {
                try {
                    // Dumping buffer
                    accBlock[blockSize++] = mAccBuffer.take().toDouble()
                    if (blockSize == Globals.ACCELEROMETER_BLOCK_CAPACITY) {
                        blockSize = 0

                        // time = System.currentTimeMillis();
                        max = .0
                        for (`val` in accBlock) {
                            if (max < `val`) {
                                max = `val`
                            }
                        }
                        fft.fft(accBlock, im)
                        for (i in accBlock.indices) {
                            val mag = Math.sqrt(accBlock[i] * accBlock[i] + im[i] * im[i])
                            im[i] = .0 // Clear the field
                            featureVector.add(mag)
                        }
                        featureVector.add(max)

                        // Append max after frequency component
                        val classifiedVal = WekaClassifier.classify( featureVector.toArray() ).toInt()
                        sendClassifyMessage(Globals.CLASS_ACTIVITY_ARRAY[classifiedVal])
                        featureVector.clear()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
    }
    //========================UPTO HERE========================//

    private fun sendClassifyMessage(any: Any) {
        try {
            val tempHandler = msgHandler
            if (tempHandler != null) {
                val bundle = Bundle()
                bundle.putString(WEKA_MSG_INT_KEY, any.toString())
                val message: Message = tempHandler.obtainMessage()
                message.data = bundle
                message.what = WEKA_MSG_INT_VALUE
                tempHandler.sendMessage(message)
                Log.d("gb", "Tracking Service Send Classify Message ${any.toString()}")
            }
        } catch (t: Throwable) {
            Log.d("TrackingService", t.toString())
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
}