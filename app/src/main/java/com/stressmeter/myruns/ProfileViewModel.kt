package com.stressmeter.myruns

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {
    // The ProfileViewModel holds the data that is displayed in the ProfileActivity's View.
    val userImage = MutableLiveData<Bitmap>()
    val username = MutableLiveData<String>()
    val email = MutableLiveData<String>()
    val phone = MutableLiveData<String>()
    val gender = MutableLiveData<Int>()
    val classYear = MutableLiveData<Int>()
    val major = MutableLiveData<String>()

}
