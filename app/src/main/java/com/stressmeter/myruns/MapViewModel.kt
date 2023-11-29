package com.stressmeter.myruns

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class MapViewModel : ViewModel(), ServiceConnection {
    private  var myMessageHandler: MyMessageHandler

    private var _bundle = MutableLiveData<Bundle>()
    val bundle: LiveData<Bundle>
        get() {
            return _bundle
        }

    private var _wekaActivity = MutableLiveData<String>()
    val wekaActivity: LiveData<String>
        get() {
            return _wekaActivity
        }

    init {
        myMessageHandler = MyMessageHandler(Looper.getMainLooper())
    }

    val avgSpeed = MutableLiveData<Double>()
    val avgPace = MutableLiveData<Double>()
    val climb = MutableLiveData<Double>()
    val curSpeed = MutableLiveData<Float>()
    val distance = MutableLiveData<Double>()
    val calorie = MutableLiveData<Double>()
    val duration = MutableLiveData<Double>()
    private var _locationList = MutableLiveData<ArrayList<LatLng>>().apply { value = arrayListOf() }
    val locationList: LiveData<ArrayList<LatLng>>
        get() {
            return _locationList
        }
    val currentLat = MutableLiveData<Double>()
    val currentLng = MutableLiveData<Double>()

    override fun onServiceConnected(name: ComponentName?, iBinder: IBinder?) {
        val tempBinder = iBinder as TrackingService.MyBinder
        tempBinder.setmsgHandler(myMessageHandler)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        return
    }

    inner class MyMessageHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            if (msg.what == TrackingService.MSG_INT_VALUE) {
                _bundle.value = msg.data
                avgSpeed.value = msg.data.getDouble("AVG_SPEED_KEY")
                avgPace.value = msg.data.getDouble("AVG_PACE_KEY")
                climb.value = msg.data.getDouble("CLIMB_KEY")
                curSpeed.value = msg.data.getFloat("CUR_SPEED_KEY")
                distance.value = msg.data.getDouble("DISTANCE_KEY")
                calorie.value = msg.data.getDouble("CALORIE_KEY")
                duration.value = msg.data.getDouble("DURATION_KEY")
                currentLat.value = msg.data.getDouble("LAT_KEY")
                currentLng.value = msg.data.getDouble("LNG_KEY")
                Log.d("gb", "Map VM ${currentLat.value} ${currentLng.value}")
                val newList = ArrayList(_locationList.value)
                newList.add(LatLng(currentLat.value!!, currentLng.value!!))
                _locationList.value = newList
            }
            if(msg.what == TrackingService.WEKA_MSG_INT_VALUE) {
                _wekaActivity.value = msg.data.getString("weka_msg_int_key")
                Log.d("gb", "Map VM ${_wekaActivity.value}")
            }
        }
    }

}
