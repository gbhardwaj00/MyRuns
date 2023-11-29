package com.stressmeter.myruns

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

@Entity(tableName = "exercise_table")
data class ExerciseEntry (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "input_type")
    var inputType: String = "",

    @ColumnInfo(name = "activity_type")
    var activityType: String = "",

    @ColumnInfo(name = "date_time")
    var dateTime: String = "",

    @ColumnInfo(name = "duration")
    var duration: Double = 0.0,

    @ColumnInfo(name = "distance")
    var distance: Double = 0.0,

    @ColumnInfo(name = "avgPace")
    var avgPace: Double = 0.0,

    @ColumnInfo(name = "avgSpeed")
    var avgSpeed: Double = 0.0,

    @ColumnInfo(name = "calorie")
    var calorie: Double = 0.0,

    @ColumnInfo(name = "climb")
    var climb: Double = 0.0,

    @ColumnInfo(name = "heart_rate")
    var heartRate: Double = 0.0,

    @ColumnInfo(name = "comment")
    var comment: String = "",

    @ColumnInfo(name = "location_list")
    var locationList: ArrayList<LatLng> = ArrayList()
)