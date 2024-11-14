package com.android.unio.model.firestore.transform

import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.user.User
import com.android.unio.model.user.UserRepositoryFirestore

fun AssociationRepositoryFirestore.Companion.serialize(association: Association): Map<String, Any> {
  return mapOf(
      "uid" to association.uid,
      "url" to association.url,
      "name" to association.name,
      "fullName" to association.fullName,
      "category" to association.category.name,
      "description" to association.description,
      "members" to association.members.uids,
      "followersCount" to association.followersCount,
      "image" to association.image,
      "events" to association.events.uids,
      "principalEmailAddress" to association.principalEmailAddress,
      "adminUid" to association.adminUid)
}

fun UserRepositoryFirestore.Companion.serialize(user: User): Map<String, Any> {
  return mapOf(
      "uid" to user.uid,
      "email" to user.email,
      "firstName" to user.firstName,
      "lastName" to user.lastName,
      "biography" to user.biography,
      "followedAssociations" to user.followedAssociations.uids,
      "savedEvents" to user.savedEvents.uids,
      "joinedAssociations" to user.joinedAssociations.uids,
      "interests" to user.interests.map { it.name },
      "socials" to user.socials.map { mapOf("social" to it.social.name, "content" to it.content) },
      "profilePicture" to user.profilePicture,
      "savedEvents" to user.savedEvents.uids)
}

fun EventRepositoryFirestore.Companion.serialize(event: Event): Map<String, Any> {
  return mapOf(
      "uid" to event.uid,
      "title" to event.title,
      "organisers" to event.organisers.uids,
      "taggedAssociations" to event.taggedAssociations.uids,
      "image" to event.image,
      "description" to event.description,
      "catchyDescription" to event.catchyDescription,
      "price" to event.price,
      "date" to event.date,
      "location" to
          mapOf(
              "latitude" to event.location.latitude,
              "longitude" to event.location.longitude,
              "name" to event.location.name),
      "types" to event.types.map { it.name })
}
