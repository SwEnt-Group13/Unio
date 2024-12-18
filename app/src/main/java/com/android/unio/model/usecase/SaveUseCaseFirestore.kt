package com.android.unio.model.usecase

import com.android.unio.model.authentication.registerAuthStateListener
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepository
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.firestore.transform.serialize
import com.android.unio.model.user.User
import com.android.unio.model.user.UserRepository
import com.android.unio.model.user.UserRepositoryFirestore
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

/**
 * A Firestore implementation of [SaveUseCase]. This class is responsible for
 * updating the Firestore database with the user's save status for an event.
 *
 * @property db The Firestore database.
 * @property userRepository The repository for user data.
 * @property eventRepository The repository for event data.
 */
class SaveUseCaseFirestore
@Inject
constructor(
    private val db: FirebaseFirestore,
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository
) : SaveUseCase {

  override fun init(onSuccess: () -> Unit) {
    Firebase.auth.registerAuthStateListener {
      if (it.currentUser != null) {
        onSuccess()
      }
    }
  }

  /**
   * Updates the Firestore database with the user's save status for an event. This operation is
   * performed atomically. If the operation fails, the database is not updated.
   *
   * @param user The user.
   * @param event The event.
   * @param onSuccess The callback that is called when the operation is successful.
   * @param onFailure The callback that is called when the operation fails.
   */
  override fun updateSave(
      user: User,
      event: Event,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.runBatch { batch ->
          val userRef = userRepository.getUserRef(user.uid)
          val eventRef = eventRepository.getEventRef(event.uid)

          batch.set(eventRef, EventRepositoryFirestore.serialize(event))
          batch.set(userRef, UserRepositoryFirestore.serialize(user))
        }
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
  }
}
