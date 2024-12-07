package com.android.unio.model.event

import androidx.appsearch.annotation.Document
import androidx.appsearch.annotation.Document.Id
import androidx.appsearch.annotation.Document.Namespace
import androidx.appsearch.annotation.Document.StringProperty
import androidx.appsearch.app.AppSearchSchema.StringPropertyConfig
import androidx.compose.ui.graphics.Color
import com.android.unio.model.association.Association
import com.android.unio.model.firestore.ReferenceList
import com.android.unio.model.firestore.UniquelyIdentifiable
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
 * @property startDate event start date
 * @property endDate event end date
 * @property location event location
 * @property types list of event types
 * @property placesRemaining max number of places available for the event
 * @property numberOfSaved number of users that saved the event
 */
data class Event(
    override var uid: String = "",
    val title: String = "",
    val organisers: ReferenceList<Association>,
    val taggedAssociations: ReferenceList<Association>,
    var image: String = "",
    val description: String = "",
    val catchyDescription: String = "",
    val price: Double = 0.0,
    val startDate: Timestamp = Timestamp(Date()),
    val endDate: Timestamp = Timestamp(Date()),
    val location: Location = Location(),
    val types: List<EventType> = mutableListOf(EventType.OTHER),
    val placesRemaining: Int = -1,
    val numberOfSaved: Int = 0
) : UniquelyIdentifiable {
  companion object
}

enum class EventType(val color: Color, val text: String) {
  FESTIVAL(eventTypeFestival, "festival"),
  APERITIF(eventTypeAperitif, "aperitif"),
  NIGHT_PARTY(eventTypeNightParty, "night party"),
  JAM(eventTypeJam, "jam"),
  NETWORKING(eventTypeNetworking, "networking"),
  SPORT(eventTypeSport, "sport"),
  TRIP(eventTypeTrip, "trip"),
  OTHER(eventTypeOther, "other")
}

@Document
data class EventDocument(
    @Namespace val namespace: String = "",
    @Id val uid: String = "",
    @StringProperty(indexingType = StringPropertyConfig.INDEXING_TYPE_PREFIXES)
    val title: String = "",
    @StringProperty(indexingType = StringPropertyConfig.INDEXING_TYPE_PREFIXES)
    val description: String,
    @StringProperty(indexingType = StringPropertyConfig.INDEXING_TYPE_PREFIXES)
    val catchyDescription: String = "",
    @StringProperty(indexingType = StringPropertyConfig.INDEXING_TYPE_PREFIXES)
    val locationName: String = ""
)

fun Event.toEventDocument(): EventDocument {
  return EventDocument(
      uid = this.uid,
      title = this.title,
      description = this.description,
      catchyDescription = this.catchyDescription,
      locationName = this.location.name)
}
