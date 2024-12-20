package com.android.unio.model.functions

import com.android.unio.model.association.Role
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.firestore.transform.serialize
import com.android.unio.model.map.Location
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

val firebaseFunctions: FirebaseFunctions by lazy { Firebase.functions }

/**
 * Retrieves the current user's token ID asynchronously.
 *
 * This function checks if the current user is signed in, and if so, retrieves their Firebase token
 * ID. If the user is not signed in, or if there is an issue fetching the token, the `onError`
 * callback is called. Otherwise, the `onSuccess` callback is invoked with the token ID.
 *
 * @param onSuccess A callback function that is called when the token ID is successfully retrieved.
 * @param onError A callback function that is called if an error occurs while retrieving the token
 *   ID.
 * @throws Exception If the user is not signed in or token retrieval fails.
 */
private fun giveCurrentUserTokenID(onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
  val currentUser = FirebaseAuth.getInstance().currentUser
  if (currentUser == null) {
    onError(IllegalStateException("User is not signed in."))
    return
  }

  currentUser.getIdToken(true).addOnCompleteListener { task ->
    if (task.isSuccessful) {
      val tokenId = task.result?.token
      if (tokenId != null) {
        onSuccess(tokenId)
      } else {
        onError(IllegalStateException("Token is null."))
      }
    } else {
      onError(task.exception ?: Exception("Failed to retrieve token ID."))
    }
  }
}

/**
 * Converts a Firebase [Timestamp] object to a formatted string in ISO 8601 format.
 *
 * This function takes a [Timestamp] object and converts it to a string formatted as
 * "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'". It ensures the timestamp is in UTC time zone.
 *
 * @param timestamp The Firebase [Timestamp] to be converted.
 * @return A string representation of the timestamp in ISO 8601 format.
 */
fun convertTimestampToString(timestamp: Timestamp): String {
  val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
  format.timeZone = TimeZone.getTimeZone("UTC") // Make sure it's in UTC
  return format.format(timestamp.toDate()) // Convert Timestamp to String (ISO 8601 format)
}

/**
 * Adds or edits an event by calling a Firebase Cloud Function to save the event.
 *
 * This function uploads event details to a Firebase Cloud Function, including the event information
 * and associated data. Depending on whether it is a new event or an update, the appropriate action
 * is taken. It retrieves the current user's token ID and sends it to the Cloud Function, along with
 * the event data.
 *
 * @param newEvent The event object to be added or updated.
 * @param associationUId The unique identifier of the association to which the event belongs.
 * @param onSuccess A callback function that is called when the event is successfully added or
 *   updated.
 * @param onError A callback function that is called if an error occurs during the process.
 * @param isNewEvent A boolean value indicating whether the event is new (true) or being edited
 *   (false).
 */
fun addEditEventCloudFunction(
    newEvent: Event,
    associationUId: String,
    onSuccess: (String) -> Unit,
    onError: (Exception) -> Unit,
    isNewEvent: Boolean
) {
  try {
      val serializedEvent = EventRepositoryFirestore.Companion.serialize(newEvent).toMutableMap()

        // Overwrite the startDate and endDate with the converted timestamp strings
      serializedEvent[Event::startDate.name] = convertTimestampToString(newEvent.startDate)
      serializedEvent[Event::endDate.name] = convertTimestampToString(newEvent.endDate)

      giveCurrentUserTokenID(
        onSuccess = { tokenId ->
          firebaseFunctions
              .getHttpsCallable("saveEvent")
              .call(
                  hashMapOf(
                      "tokenId" to tokenId,
                      "event" to serializedEvent,
                      "isNewEvent" to isNewEvent,
                      "associationUid" to associationUId))
              .addOnSuccessListener { result ->
                val responseData = result.data as? String
                if (responseData != null) {
                  onSuccess(responseData)
                } else {
                  onError(IllegalStateException("Unexpected response format from Cloud Function."))
                }
              }
              .addOnFailureListener { error -> onError(error) }
        },
        onError = { error -> onError(error) })
  } catch (e: Exception) {
    onError(e)
  }
}

/**
 * Adds or edits a role by calling a Firebase Cloud Function to save the role.
 *
 * This function uploads role details to a Firebase Cloud Function, including role-specific
 * information and permissions. It retrieves the current user's token ID and sends it to the Cloud
 * Function, along with the role data.
 *
 * @param newRole The role object to be added or updated.
 * @param associationUId The unique identifier of the association to which the role belongs.
 * @param onSuccess A callback function that is called when the role is successfully added or
 *   updated.
 * @param onError A callback function that is called if an error occurs during the process.
 * @param isNewRole A boolean value indicating whether the role is new (true) or being edited
 *   (false).
 */
fun addEditRoleCloudFunction(
    newRole: Role,
    associationUId: String,
    onSuccess: (String) -> Unit,
    onError: (Exception) -> Unit,
    isNewRole: Boolean
) {
  try {
    giveCurrentUserTokenID(
        onSuccess = { tokenId ->
          firebaseFunctions
              .getHttpsCallable("saveRole")
              .call(
                  hashMapOf(
                      "tokenId" to tokenId,
                      "role" to
                          mapOf(
                              "displayName" to newRole.displayName,
                              "permissions" to
                                  newRole.permissions.getGrantedPermissions().toList().map {
                                      permission ->
                                    permission.stringName
                                  },
                              "color" to newRole.color.toInt(),
                              "uid" to newRole.uid),
                      "isNewRole" to isNewRole,
                      "associationUid" to associationUId))
              .addOnSuccessListener { result ->
                val responseData = result.data as? String
                if (responseData != null) {
                  onSuccess(responseData)
                } else {
                  onError(IllegalStateException("Unexpected response format from Cloud Function."))
                }
              }
              .addOnFailureListener { error -> onError(error) }
        },
        onError = { error -> onError(error) })
  } catch (e: Exception) {
    onError(e)
  }
}
