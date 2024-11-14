package com.android.unio.model.firestore.transform

import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationCategory
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.event.EventType
import com.android.unio.model.firestore.firestoreReferenceListWith
import com.android.unio.model.map.Location
import com.android.unio.model.user.Interest
import com.android.unio.model.user.Social
import com.android.unio.model.user.User
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserSocial
import com.google.firebase.Timestamp

fun AssociationRepositoryFirestore.Companion.hydrate(data: Map<String, Any>?): Association {
  val category = data?.get(Association::category.name)
  val memberUids = data?.get(Association::members.name) as? List<String> ?: emptyList()
  val members = User.firestoreReferenceListWith(memberUids)

  val events =
      Event.firestoreReferenceListWith(
          data?.get(Association::events.name) as? List<String> ?: emptyList())

  val parentAssociations =
      Association.firestoreReferenceListWith(
          data?.get("parentAssociations") as? List<String> ?: emptyList())
  val childAssociations =
      Association.firestoreReferenceListWith(
          data?.get("childAssociations") as? List<String> ?: emptyList())

  return Association(
      uid = data?.get(Association::uid.name) as? String ?: "",
      url = data?.get(Association::url.name) as? String ?: "",
      name = data?.get(Association::name.name) as? String ?: "",
      fullName = data?.get(Association::fullName.name) as? String ?: "",
      category =
          if (category is String) AssociationCategory.valueOf(category)
          else AssociationCategory.UNKNOWN,
      description = data?.get(Association::description.name) as? String ?: "",
      members = members,
      followersCount = (data?.get(Association::followersCount.name) as? Number ?: 0).toInt(),
      image = data?.get(Association::image.name) as? String ?: "",
      events = events,
      principalEmailAddress = data?.get(Association::principalEmailAddress.name) as? String ?: "",
      adminUid = data?.get(Association::adminUid.name) as? String ?: "",
  )
}

fun UserRepositoryFirestore.Companion.hydrate(data: Map<String, Any>?): User {
  val followedAssociationsUids =
      data?.get(User::followedAssociations.name) as? List<String> ?: emptyList()
  val followedAssociations = Association.firestoreReferenceListWith(followedAssociationsUids)

  val savedEventsUids = data?.get(User::savedEvents.name) as? List<String> ?: emptyList()
  val savedEvents = Event.firestoreReferenceListWith(savedEventsUids)

  val joinedAssociationsUids =
      data?.get(User::joinedAssociations.name) as? List<String> ?: emptyList()
  val joinedAssociations = Association.firestoreReferenceListWith(joinedAssociationsUids)

  return User(
      uid = data?.get(User::uid.name) as? String ?: "",
      email = data?.get(User::email.name) as? String ?: "",
      firstName = data?.get(User::firstName.name) as? String ?: "",
      lastName = data?.get(User::lastName.name) as? String ?: "",
      biography = data?.get(User::biography.name) as? String ?: "",
      followedAssociations = followedAssociations,
      savedEvents = savedEvents,
      joinedAssociations = joinedAssociations,
      interests =
          (data?.get(User::interests.name) as? List<String> ?: emptyList()).map {
            Interest.valueOf(it)
          },
      socials =
          (data?.get(User::socials.name) as? List<Map<String, String>> ?: emptyList()).map {
            UserSocial(
                Social.valueOf(it[UserSocial::social.name] ?: ""),
                it[UserSocial::content.name] ?: "")
          },
      profilePicture = data?.get(User::profilePicture.name) as? String ?: "")
}

fun EventRepositoryFirestore.Companion.hydrate(data: Map<String, Any>?): Event {
  val organisers =
      Association.firestoreReferenceListWith(
          data?.get(Event::organisers.name) as? List<String> ?: emptyList())

  val taggedAssociations =
      Association.firestoreReferenceListWith(
          data?.get(Event::taggedAssociations.name) as? List<String> ?: emptyList())

  val types = (data?.get(Event::types.name) as? List<String> ?: emptyList())

  val location = data?.get(Event::location.name) as? Map<String, Any> ?: emptyMap()

  return Event(
      uid = data?.get(Event::uid.name) as? String ?: "",
      title = data?.get(Event::title.name) as? String ?: "",
      organisers = organisers,
      taggedAssociations = taggedAssociations,
      image = data?.get(Event::image.name) as? String ?: "",
      description = data?.get(Event::description.name) as? String ?: "",
      catchyDescription = data?.get(Event::catchyDescription.name) as? String ?: "",
      price = data?.get(Event::price.name) as? Double ?: 0.0,
      date = data?.get(Event::date.name) as? Timestamp ?: Timestamp(0, 0),
      location =
          Location(
              latitude = location.get(Location::latitude.name) as? Double ?: 0.0,
              longitude = location.get(Location::longitude.name) as? Double ?: 0.0,
              name = location.get(Location::name.name) as? String ?: ""),
      types = types.map { EventType.valueOf(it) },
      placesRemaining = data?.get(Event::placesRemaining.name) as? Int ?: -1)
}
