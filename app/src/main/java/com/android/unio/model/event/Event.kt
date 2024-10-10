package com.android.unio.model.event

import com.android.unio.model.association.Association
import com.android.unio.model.firestore.ReferenceList
import com.android.unio.model.map.Location
import com.google.firebase.Timestamp
import java.util.Date

data class Event(
    val uid: String = "",
    val title: String = "",
    val organisers: ReferenceList<Association>,
    val taggedAssociations: ReferenceList<Association>,
    val image: String = "",
    val description: String = "",
    val catchyDescription: String = "",
    val price: Double = 0.0,
    val date: Timestamp = Timestamp(Date()),
    val location: Location = Location(),
    val types: List<EventType> = mutableListOf<EventType>()
)
