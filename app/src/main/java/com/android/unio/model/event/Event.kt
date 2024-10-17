package com.android.unio.model.event

import androidx.appsearch.annotation.Document
import androidx.appsearch.annotation.Document.Id
import androidx.appsearch.annotation.Document.Namespace
import androidx.appsearch.annotation.Document.StringProperty
import androidx.appsearch.app.AppSearchSchema.StringPropertyConfig
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
    //TODO Need to add organisers and taggedAssociations
)

fun Event.toEventDocument(): EventDocument {
  return EventDocument(
      uid = this.uid,
      title = this.title,
      description = this.description,
      catchyDescription = this.catchyDescription,
      locationName = this.location.name
      // Include other fields as needed
      )
}
