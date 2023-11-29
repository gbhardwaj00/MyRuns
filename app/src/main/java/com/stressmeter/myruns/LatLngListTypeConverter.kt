package com.stressmeter.myruns

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LatLngListTypeConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromLatLngList(list: ArrayList<LatLng>): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toLatLngList(data: String?): ArrayList<LatLng> {
        if (data == null || data.isEmpty()) {
            return arrayListOf()
        }
        val listType = object : TypeToken<ArrayList<LatLng>>() {}.type
        return gson.fromJson(data, listType) ?: arrayListOf()
    }
}
