package com.android.unio.model.functions

import android.util.Log
import com.android.unio.model.association.Role
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.firestore.transform.serialize
import com.android.unio.model.map.Location
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Retrieves the current user's token ID asynchronously.
 * @return The user's token ID as a String.
 * @throws Exception if the user is not signed in or the token retrieval fails.
 */
private fun giveCurrentUserTokenID(
    onSuccess: (String) -> Unit,
    onError: (Exception) -> Unit
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser == null) {
        onError(IllegalStateException("User is not signed in."))
        return
    }

    currentUser.getIdToken(true)
        .addOnCompleteListener { task ->
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

fun convertTimestampToString(timestamp: Timestamp): String {
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    format.timeZone = TimeZone.getTimeZone("UTC") // Make sure it's in UTC
    return format.format(timestamp.toDate())  // Convert Timestamp to String (ISO 8601 format)
}

fun addEditEventCloudFunction(
    newEvent: Event,
    associationUId: String,
    onSuccess: (String) -> Unit,
    onError: (Exception) -> Unit,
    isNewEvent: Boolean
) {
    try {
        // Fetch the token asynchronously
        giveCurrentUserTokenID(
            onSuccess = { tokenId ->
                Log.d("EventCreation", "Token ID: $tokenId")

                // Call the Firebase Cloud Function
                Firebase.functions
                    .getHttpsCallable("saveEvent")
                    .call(
                        hashMapOf(
                            "tokenId" to tokenId,
                            "event" to mapOf(
                                Event::uid.name to newEvent.uid,
                                Event::title.name to newEvent.title,
                                Event::organisers.name to newEvent.organisers.uids,
                                Event::taggedAssociations.name to newEvent.taggedAssociations.uids,
                                Event::image.name to newEvent.image,
                                Event::description.name to newEvent.description,
                                Event::catchyDescription.name to newEvent.catchyDescription,
                                Event::price.name to newEvent.price,
                                Event::startDate.name to convertTimestampToString(newEvent.startDate), // Convert to milliseconds since epoch
                                Event::endDate.name to convertTimestampToString(newEvent.endDate),     // Convert to milliseconds since epoch
                                Event::location.name to mapOf(
                                    Location::latitude.name to newEvent.location.latitude,
                                    Location::longitude.name to newEvent.location.longitude,
                                    Location::name.name to newEvent.location.name
                                ),
                                Event::types.name to newEvent.types.map { it.name },
                                Event::maxNumberOfPlaces.name to newEvent.maxNumberOfPlaces,
                                Event::numberOfSaved.name to newEvent.numberOfSaved,
                                Event::eventPictures.name to newEvent.eventPictures.uids
                            ),
                            "isNewEvent" to isNewEvent,
                            "associationUid" to associationUId
                        )
                    )
                    .addOnSuccessListener { result ->
                        val responseData = result.data as? String
                        if (responseData != null) {
                            onSuccess(responseData)
                        } else {
                            onError(IllegalStateException("Unexpected response format from Cloud Function."))
                        }
                    }
                    .addOnFailureListener { error ->
                        onError(error)
                    }
            },
            onError = { error ->
                onError(error)
            }
        )
    } catch (e: Exception) {
        onError(e)
    }
}

fun addEditRoleCloudFunction(
    newRole: Role,
    associationUId: String,
    onSuccess: (String) -> Unit,
    onError: (Exception) -> Unit,
    isNewRole : Boolean
) {
    try {
        // Fetch the token asynchronously
        giveCurrentUserTokenID(
            onSuccess = { tokenId ->
                Log.d("addRoleTQT", "Token ID: $tokenId")

                // Call the Firebase Cloud Function
                Firebase.functions
                    .getHttpsCallable("saveRole")
                    .call(
                        hashMapOf(
                            "tokenId" to tokenId,
                            "role" to mapOf(
                                "displayName" to newRole.displayName,
                                "permissions" to newRole.permissions.getGrantedPermissions().toList()
                                    .map { permission -> permission.stringName },
                                "color" to newRole.color.toInt(),
                                "uid" to newRole.uid
                            ),
                            "isNewRole" to isNewRole,
                            "associationUid" to associationUId
                        )
                    )
                    .addOnSuccessListener { result ->
                        val responseData = result.data as? String
                        if (responseData != null) {
                            onSuccess(responseData)
                        } else {
                            onError(IllegalStateException("Unexpected response format from Cloud Function."))
                        }
                    }
                    .addOnFailureListener { error ->
                        onError(error)
                    }
            },
            onError = { error ->
                onError(error)
            }
        )
    } catch (e: Exception) {
        onError(e)
    }
}