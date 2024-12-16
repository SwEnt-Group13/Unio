package com.android.unio.model.user

import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationRepository
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.authentication.registerAuthStateListener
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepository
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.firestore.transform.serialize
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class UserDeletionRepositoryFirestore
@Inject
constructor(
    private val db: FirebaseFirestore,
    private val userRepository: UserRepository,
    private val associationRepository: AssociationRepository,
    private val eventRepository: EventRepository
) : UserDeletionRepository {

  override fun init(onSuccess: () -> Unit) {
    Firebase.auth.registerAuthStateListener {
      if (it.currentUser != null) {
        onSuccess()
      }
    }
  }

  override fun deleteUser(
      userId: String,
      eventToUpdate: List<Event>,
      associationToUpdate: List<Association>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.runBatch { batch ->
      val userRef = userRepository.getUserRef(userId)

      eventToUpdate.forEach { event ->
        val eventRef = eventRepository.getEventRef(event.uid)
        batch.set(eventRef, EventRepositoryFirestore.serialize(event))
      }

      associationToUpdate.forEach { association ->
        val assoRef = associationRepository.getAssociationRef(association.uid, false)
        batch.set(assoRef, AssociationRepositoryFirestore.serialize(association))
      }

      batch.delete(userRef)
    }
  }
}
