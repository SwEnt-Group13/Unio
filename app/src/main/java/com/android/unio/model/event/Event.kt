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
import com.android.unio.ui.theme.EventColors
import com.android.unio.R
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
 * @property maxNumberOfPlaces max number of places available for the event
 * @property numberOfSaved number of users that saved the event
 * * @property eventPictures number of remaining places
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
    val types: List<EventType>,
    val maxNumberOfPlaces: Int = -1,
    val numberOfSaved: Int = 0,
    val eventPictures: ReferenceList<EventUserPicture>,
) : UniquelyIdentifiable {
  companion object
}

/**
 * Enum class that represents the different types of events
 *
 * @property color event type color
 * @property text event type text
 */
enum class EventType(val color: Color, val text: Int) {
  FESTIVAL(EventColors.Festival, R.string.event_type_festival), // + Music and Festivals
  APERITIF(EventColors.Aperitif, R.string.event_type_aperitif), // + Food and Apéro
  NIGHT_PARTY(EventColors.NightParty, R.string.event_type_night_party), // + Music and Festivals
  JAM(EventColors.Jam, R.string.event_type_jam), // + Music and Art
  NETWORKING(EventColors.Networking, R.string.event_type_networking), // + Apéro and Networking
  SPORT_TOURNAMENT(EventColors.SportTournament, R.string.event_type_sport_tournament), // + Sports
    SPORT_DISCOVERY(EventColors.SportDiscovery, R.string.event_type_sport_discovery), // + Sports, Socialising
  TRIP(EventColors.Trip, R.string.event_type_trip), // + Travel, Culture
    LAN(EventColors.Lan, R.string.event_type_lan), // + Gaming
    FILM_PROJECTION(EventColors.FilmProjection, R.string.event_type_film_projection), // + Art, Culture
    FOREIGN_CULTURE_DISCOVERY(EventColors.ForeignCultureDiscovery, R.string.event_type_foreign_culture_discovery), // + Culture, Literature
    TECH_PRESENTATION(EventColors.TechPresentation, R.string.event_type_tech_presentation), // + Tech, Science
    SCIENCE_FARE(EventColors.ScienceFare, R.string.event_type_science_fare), // + Science, Tech
    FOOD_DISTRIBUTION(EventColors.FoodDistribution, R.string.event_type_food_distribuition), // + Food
    ART_CONVENTION(EventColors.ArtConvention, R.string.event_type_art_convention), // + Art, Literature
    MANIFESTATION(EventColors.Manifestation, R.string.event_type_manifestation), // + Culture, Socialising
    BOARD_GAMES(EventColors.BoardGames, R.string.event_type_board_games), // + Gaming, Socialising
    GROUP_STUDY(EventColors.GroupStudy, R.string.event_type_group_study), // + Science, Tech
    OTHER(EventColors.Other, R.string.event_type_other)
}

/**
 * A class representing the event document for AppSearch indexing. It allows the search engine to
 * search on the event title, description, catchy description and location name.
 *
 * @property namespace namespace of the event document
 * @property uid event id
 * @property title event title
 * @property description event description
 * @property catchyDescription event catchy description
 * @property locationName event location name
 */
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

/**
 * Extension function to convert an Event object to an EventDocument object
 *
 * @return EventDocument object
 */
fun Event.toEventDocument(): EventDocument {
  return EventDocument(
      uid = this.uid,
      title = this.title,
      description = this.description,
      catchyDescription = this.catchyDescription,
      locationName = this.location.name)
}
