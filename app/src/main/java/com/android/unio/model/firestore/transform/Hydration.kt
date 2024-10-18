package com.android.unio.model.firestore.transform

import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationCategory
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.event.EventType
import com.android.unio.model.firestore.FirestorePaths.ASSOCIATION_PATH
import com.android.unio.model.firestore.FirestorePaths.USER_PATH
import com.android.unio.model.firestore.FirestoreReferenceList
import com.android.unio.model.map.Location
import com.android.unio.model.user.Interest
import com.android.unio.model.user.Social
import com.android.unio.model.user.User
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserSocial
import com.google.firebase.Timestamp

fun AssociationRepositoryFirestore.Companion.hydrate(data: Map<String, Any>?): Association {
  val category = data?.get("category")
  val memberUids = data?.get("members") as? List<String> ?: emptyList()
  val members = User.firestoreReferenceListWith(memberUids)

  return Association(
      uid = data?.get("uid") as? String ?: "",
      url = data?.get("url") as? String ?: "",
      name = data?.get("name") as? String ?: "",
      fullName = data?.get("fullName") as? String ?: "",
      category =
          if (category is String) AssociationCategory.valueOf(category)
          else AssociationCategory.UNKNOWN,
      description = data?.get("description") as? String ?: "",
      members = members,
      image = data?.get("image") as? String ?: "")
}

fun UserRepositoryFirestore.Companion.hydrate(data: Map<String, Any>?): User {
  val followingAssociationsUids = data?.get("followingAssociations") as? List<String> ?: emptyList()
  val followingAssociations = Association.firestoreReferenceListWith(followingAssociationsUids)

  val savedEventsUids = data?.get("savedEvents") as? List<String> ?: emptyList()
  val savedEvents =
      FirestoreReferenceList.fromList(
          savedEventsUids,
          Firebase.firestore.collection(EVENT_PATH),
          EventRepositoryFirestore::hydrate)

  return User(
      uid = data?.get("uid") as? String ?: "",
      email = data?.get("email") as? String ?: "",
      followingAssociations = followingAssociations)
}

fun EventRepositoryFirestore.Companion.hydrate(data: Map<String, Any>?): Event {
  val organisers =
      Association.firestoreReferenceListWith(
          data?.get("organisers") as? List<String> ?: emptyList())

  val taggedAssociations =
      Association.firestoreReferenceListWith(
          data?.get("taggedAssociations") as? List<String> ?: emptyList())

  val types = (data?.get("types") as? List<String> ?: emptyList())

  val location = data?.get("location") as? Map<String, Any> ?: emptyMap()

  return Event(
      uid = data?.get("uid") as? String ?: "",
      title = data?.get("title") as? String ?: "",
      organisers = organisers,
      taggedAssociations = taggedAssociations,
      image = data?.get("image") as? String ?: "",
      description = data?.get("description") as? String ?: "",
      catchyDescription = data?.get("catchyDescription") as? String ?: "",
      price = data?.get("price") as? Double ?: 0.0,
      date = data?.get("date") as? Timestamp ?: Timestamp(0, 0),
      location =
          Location(
              latitude = location.get("latitude") as? Double ?: 0.0,
              longitude = location.get("longitude") as? Double ?: 0.0,
              name = location.get("name") as? String ?: ""),
      types = types.map { EventType.valueOf(it) })
}
