package com.android.unio.model.event

import androidx.compose.ui.graphics.Color
import com.android.unio.model.association.Association
import com.android.unio.model.firestore.ReferenceList
import com.android.unio.model.map.Location
import com.android.unio.ui.theme.eventTypeAperitif
import com.android.unio.ui.theme.eventTypeFestival
import com.android.unio.ui.theme.eventTypeJam
import com.android.unio.ui.theme.eventTypeNetworking
import com.android.unio.ui.theme.eventTypeNightParty
import com.android.unio.ui.theme.eventTypeOther
import com.android.unio.ui.theme.eventTypeSport
import com.android.unio.ui.theme.eventTypeTrip
import com.google.firebase.Timestamp
import java.util.Date

/**
 * Event data class Make sure to update the hydration and serialization methods when changing the
 * data class
 *
 * @property uid event id
 * @property title event title
 * @property organisers list of associations that are organising the event
 * @property taggedAssociations list of associations that are tagged in the event
 * @property image event image
 * @property description event description
 * @property catchyDescription event catchy description
 * @property price event price
 * @property date event date
 * @property location event location
 * @property types list of event types
 */
data class Event(
    var uid: String = "",
    val title: String = "",
    val organisers: ReferenceList<Association>,
    val taggedAssociations: ReferenceList<Association>,
    var image: String = "",
    val description: String = "",
    val catchyDescription: String = "",
    val price: Double = 0.0,
    val date: Timestamp = Timestamp(Date()),
    val location: Location = Location(),
    val types: List<EventType> = mutableListOf(EventType.OTHER)
) {
  companion object
}
