package com.android.unio.model.firestore.transform

import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationCategory
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.association.Member
import com.android.unio.model.association.PermissionType
import com.android.unio.model.association.Permissions
import com.android.unio.model.association.Role
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
import firestoreReferenceElementWith

// This file contains extension functions for hydrating Firestore data into model objects.

/**
 * Hydrates Firestore [Association] data into model objects.
 *
 * @param data Firestore data to hydrate.
 * @return Hydrated Association object.
 */
fun AssociationRepositoryFirestore.Companion.hydrate(data: Map<String, Any>?): Association {
  val category = data?.get(Association::category.name)
  val events =
      Event.firestoreReferenceListWith(
          data?.get(Association::events.name) as? List<String> ?: emptyList())
  val rolesMap = data?.get(Association::roles.name) as? Map<String, Map<String, Any>> ?: emptyMap()
  val permissions = Permissions.NONE
  val roles =
      rolesMap.map { (roleUid, roleData) ->
        Role(
            uid = roleUid,
            displayName = roleData[Role::displayName.name] as? String ?: "",
            permissions =
                permissions.addPermissions(
                    (roleData[Role::permissions.name] as? List<String> ?: emptyList()).mapNotNull {
                        permissionString ->
                      // Find the corresponding PermissionType by its stringName
                      PermissionType.entries.find { it.stringName == permissionString }
                    }))
      }

  // Hydrate members
  val membersMap = data?.get(Association::members.name) as? Map<String, String> ?: emptyMap()
  val memberReferences =
      membersMap.map { (userUid, roleUid) ->
        // Create a ReferenceElement for the User, which can be lazily fetched
        val userReference = User.firestoreReferenceElementWith(userUid)
        val role = roles.firstOrNull { it.uid == roleUid } ?: Role.GUEST

        // Return a Member containing the ReferenceElement<User> and the associated Role
        Member(user = userReference, role = role)
      }

  return Association(
      uid = data?.get(Association::uid.name) as? String ?: "",
      url = data?.get(Association::url.name) as? String ?: "",
      name = data?.get(Association::name.name) as? String ?: "",
      fullName = data?.get(Association::fullName.name) as? String ?: "",
      category =
          if (category is String) AssociationCategory.valueOf(category)
          else AssociationCategory.UNKNOWN,
      description = data?.get(Association::description.name) as? String ?: "",
      members = memberReferences,
      followersCount = (data?.get(Association::followersCount.name) as? Number ?: 0).toInt(),
      image = data?.get(Association::image.name) as? String ?: "",
      events = events,
      principalEmailAddress = data?.get(Association::principalEmailAddress.name) as? String ?: "",
      roles = roles,
  )
}

/**
 * Hydrates Firestore [User] data into model objects.
 *
 * @param data Firestore data to hydrate.
 * @return Hydrated User object.
 */
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

/**
 * Hydrates Firestore [Event] data into model objects.
 *
 * @param data Firestore data to hydrate.
 * @return Hydrated Event object.
 */
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
      startDate = data?.get(Event::startDate.name) as? Timestamp ?: Timestamp(0, 0),
      endDate = data?.get(Event::endDate.name) as? Timestamp ?: Timestamp(0, 0),
      location =
          Location(
              latitude = location.get(Location::latitude.name) as? Double ?: 0.0,
              longitude = location.get(Location::longitude.name) as? Double ?: 0.0,
              name = location.get(Location::name.name) as? String ?: ""),
      types = types.map { EventType.valueOf(it) },
      maxNumberOfPlaces = (data?.get(Event::maxNumberOfPlaces.name) as? Number ?: -1).toInt(),
      numberOfSaved = (data?.get(Event::numberOfSaved.name) as? Number ?: 0).toInt())
}
