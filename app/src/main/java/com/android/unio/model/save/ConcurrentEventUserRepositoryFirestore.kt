package com.android.unio.model.save

import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.authentication.registerAuthStateListener
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepository
import com.android.unio.model.firestore.transform.serialize
import com.android.unio.model.user.User
import com.android.unio.model.user.UserRepository
import com.android.unio.model.user.UserRepositoryFirestore
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

/**
 * A Firestore implementation of [ConcurrentEventUserRepository]. This class is responsible for
 * updating the Firestore database with the user's save status for an event.
 *
 * @property db The Firestore database.
 * @property userRepository The repository for user data.
 * @property eventRepository The repository for event data.
 */
class ConcurrentEventUserRepositoryFirestore
@Inject
constructor(
    private val db: FirebaseFirestore,
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository
) : ConcurrentEventUserRepository {
  override fun init(onSuccess: () -> Unit) {
    Firebase.auth.registerAuthStateListener {
      if (it.currentUser != null) {
        onSuccess()
      }
    }
  }

  override fun updateSave(
      user: User,
      event: Event,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.runBatch { batch ->
          val userRef = userRepository.getUserRef(user.uid)
          val associationRef = eventRepository.getEventRef(association.uid)

          batch.set(associationRef, AssociationRepositoryFirestore.serialize(association))
          batch.set(userRef, UserRepositoryFirestore.serialize(user))
        }
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
  }
}
