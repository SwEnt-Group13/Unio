package com.android.unio.model.user

import android.util.Log
import com.android.unio.model.firestore.FirestorePaths.USER_PATH
import com.android.unio.model.firestore.transform.hydrate
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class UserRepositoryFirestore(private val db: FirebaseFirestore) : UserRepository {

  override fun init(onSuccess: () -> Unit) { //repository is only considered "initialized" when a user is authenticated
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
          val users = result.map { hydrate(it.data) }
          onSuccess(users)
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
          val user = hydrate(document.data)
          onSuccess(user)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }


    override fun saveEvent(userUid: String, eventUid: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userDocumentRef = Firebase.firestore.collection("users").document(userUid)

        userDocumentRef.update("savedEvents", FieldValue.arrayUnion(eventUid))
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }


    override fun unsaveEvent(
        userUid: String,
        eventUid: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userDocRef = Firebase.firestore.collection("users").document(userUid)
        userDocRef.update("savedEvents", FieldValue.arrayRemove(eventUid))
            .addOnSuccessListener {
                Log.d("Firestore", "Event successfully removed from saved events for user: $userUid")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error removing event for user: $userUid", e)
                onFailure(e)
            }
    }

     override fun isEventSaved(userUid: String, eventUid: String, onResult: (Boolean) -> Unit) {
        val userDocRef = Firebase.firestore.collection("users").document(userUid)
        userDocRef.get()
            .addOnSuccessListener { document ->
                val savedEvents = document.get("savedEvents") as? List<String> ?: emptyList()
                onResult(savedEvents.contains(eventUid)) // return boolean based on presence
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error checking saved events for user: $userUid", e)
                onResult(false) // if any error return false
            }
    }


     override fun listenToSavedEvents(userUid: String, onSavedEventsChanged: (List<String>) -> Unit) {
        val userDocRef = Firebase.firestore.collection("users").document(userUid)
        userDocRef.addSnapshotListener { documentSnapshot, e ->
            if (e != null) {
                Log.w("Firestore", "Listen failed.", e)
                return@addSnapshotListener
            }
            if (documentSnapshot != null && documentSnapshot.exists()) {
                val savedEvents = documentSnapshot.get("savedEvents") as? List<String> ?: emptyList()
                onSavedEventsChanged(savedEvents)
            } else {
                Log.d("Firestore", "Current data: null")
                onSavedEventsChanged(emptyList())
            }
        }
    }




  companion object
}
