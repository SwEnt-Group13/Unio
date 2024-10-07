package com.android.unio.model.event

import com.android.unio.model.map.Location
import com.google.firebase.Timestamp
import java.util.Date

data class Event(
    val uid: String = "",
    val title: String = "",
    val organisers: List<String> = mutableListOf<String>(),
    val image: String = "", // TODO: determine what format is the best
    val description: String = "",
    val price: Double = 0.0,
    val date: Timestamp = Timestamp(Date()),
    val location: Location = Location()
)
