package com.android.unio.model.user

import com.android.unio.model.firestore.FirestorePaths.USER_PATH
import com.android.unio.model.firestore.transform.hydrate
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.map

class UserRepositoryFirestore(private val db: FirebaseFirestore) : UserRepository {

  override fun init(onSuccess: () -> Unit) {
    Firebase.auth.addAuthStateListener {
      if (it.currentUser != null) {
        onSuccess()
      }
    }
  }

  override fun getUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(USER_PATH)
        .get()
        .addOnSuccessListener { result ->
          val associations = result.map { hydrate(it.data) }
          onSuccess(associations)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun getUserWithId(
      id: String,
      onSuccess: (User) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(USER_PATH)
        .document(id)
        .get()
        .addOnSuccessListener { document ->
          val association = hydrate(document.data)
          onSuccess(association)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun saveEvent(
      userId: String,
      eventId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {

    db.collection(USER_PATH)
        .document(userId)
        .get()
        .addOnSuccessListener { document ->
          if (document.exists()) {

            val user = hydrate(document.data)

            val currentSavedEvents = user.savedEvents.list.value.map { ev -> ev.uid }
            if (eventId in currentSavedEvents) {
              onSuccess()
              return@addOnSuccessListener
            }

            // Add the event ID to the list of saved events
            val updatedSavedEvents = currentSavedEvents.toMutableList()
            updatedSavedEvents.add(eventId)

            // Update Firestore with the new saved events list
            db.collection(USER_PATH)
                .document(userId)
                .update("savedEvents", updatedSavedEvents)
                .addOnSuccessListener {
                  onSuccess() // Call onSuccess when the update is successful
                }
                .addOnFailureListener { exception ->
                  onFailure(exception) // Handle failure to update Firestore
                }
          } else {
            // User document does not exist
            onFailure(Exception("User not found"))
          }
        }
        .addOnFailureListener { exception ->
          onFailure(exception) // Handle failure to retrieve user document
        }
  }

  // Note: the following line is needed to add external methods to the companion object
  companion object
}
