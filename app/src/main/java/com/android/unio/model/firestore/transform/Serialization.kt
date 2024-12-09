package com.android.unio.model.firestore.transform

import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.association.Member
import com.android.unio.model.association.Role
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.map.Location
import com.android.unio.model.user.User
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserSocial

// This file contains extension functions for serializing model objects into Firestore data.

/**
 * Serializes [Association] data into Firestore data.
 *
 * @param association Association to serialize.
 * @return Serialized Firestore data.
 */
fun AssociationRepositoryFirestore.Companion.serialize(association: Association): Map<String, Any> {
  println(mapRolesToPermission(association.roles))
  return mapOf(
      Association::uid.name to association.uid,
      Association::url.name to association.url,
      Association::name.name to association.name,
      Association::fullName.name to association.fullName,
      Association::category.name to association.category.name,
      Association::description.name to association.description,
      Association::members.name to mapUsersToRoles(association.members),
      Association::roles.name to mapRolesToPermission(association.roles),
      Association::followersCount.name to association.followersCount,
      Association::image.name to association.image,
      Association::events.name to association.events.uids,
      Association::principalEmailAddress.name to association.principalEmailAddress)
}

/**
 * Maps a list of [Member] to a map of user UIDs to role UIDs.
 *
 * @param members Members to map.
 * @return Map of user UIDs to role UIDs.
 */
fun mapUsersToRoles(members: List<Member>): Map<String, String> {
  return members.associate { member -> member.user.uid to member.role.uid }
}

/**
 * Maps a list of [Role] to a map of role UIDs to role data.
 *
 * @param roles Roles to map.
 * @return Map of role UIDs to role data.
 */
fun mapRolesToPermission(roles: List<Role>): Map<String, Map<String, Any>> {
  return roles.associate { role ->
    role.uid to
        mapOf(
            Role::displayName.name to role.displayName,
            Role::permissions.name to
                role.permissions.getGrantedPermissions().map { it.stringName })
  }
}

/**
 * Serializes [User] data into Firestore data.
 *
 * @param user User to serialize.
 * @return Serialized Firestore data.
 */
fun UserRepositoryFirestore.Companion.serialize(user: User): Map<String, Any> {
  return mapOf(
      User::uid.name to user.uid,
      User::email.name to user.email,
      User::firstName.name to user.firstName,
      User::lastName.name to user.lastName,
      User::biography.name to user.biography,
      User::followedAssociations.name to user.followedAssociations.uids,
      User::savedEvents.name to user.savedEvents.uids,
      User::joinedAssociations.name to user.joinedAssociations.uids,
      User::interests.name to user.interests.map { it.name },
      User::socials.name to
          user.socials.map {
            mapOf(UserSocial::social.name to it.social.name, UserSocial::content.name to it.content)
          },
      User::profilePicture.name to user.profilePicture,
      User::savedEvents.name to user.savedEvents.uids)
}

/**
 * Serializes [Event] data into Firestore data.
 *
 * @param event Event to serialize.
 * @return Serialized Firestore data.
 */
fun EventRepositoryFirestore.Companion.serialize(event: Event): Map<String, Any> {
  return mapOf(
      Event::uid.name to event.uid,
      Event::title.name to event.title,
      Event::organisers.name to event.organisers.uids,
      Event::taggedAssociations.name to event.taggedAssociations.uids,
      Event::image.name to event.image,
      Event::description.name to event.description,
      Event::catchyDescription.name to event.catchyDescription,
      Event::price.name to event.price,
      Event::startDate.name to event.startDate,
      Event::endDate.name to event.endDate,
      Event::location.name to
          mapOf(
              Location::latitude.name to event.location.latitude,
              Location::longitude.name to event.location.longitude,
              Location::name.name to event.location.name),
      Event::types.name to event.types.map { it.name },
      Event::maxNumberOfPlaces.name to event.maxNumberOfPlaces,
      Event::numberOfSaved.name to event.numberOfSaved)
}
