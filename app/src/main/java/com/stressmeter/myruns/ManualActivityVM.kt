package com.stressmeter.myruns

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ManualActivityVM : ViewModel(){
    val date = MutableLiveData<String>()
    val time = MutableLiveData<String>()
    val duration = MutableLiveData<Double>()
    val distance = MutableLiveData<Double>()
    val calories = MutableLiveData<Double>()
    val heartRate = MutableLiveData<Double>()
    val comment = MutableLiveData<String>()

}

