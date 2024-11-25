package com.android.unio.model.follow

import android.util.Log
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationRepository
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.authentication.registerAuthStateListener
import com.android.unio.model.firestore.transform.serialize
import com.android.unio.model.user.User
import com.android.unio.model.user.UserRepository
import com.android.unio.model.user.UserRepositoryFirestore
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class ConcurrentAssociationUserRepositoryFirestore
@Inject
constructor(
    private val db: FirebaseFirestore,
    private val userRepository: UserRepository,
    private val associationRepository: AssociationRepository
) : ConcurrentAssociationUserRepository {

  override fun init(onSuccess: () -> Unit) {
    Firebase.auth.registerAuthStateListener {
      if (it.currentUser != null) {
        onSuccess()
      }
    }
  }

  override fun updateFollow(
      user: User,
      association: Association,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.runBatch { batch ->
          val userRef = userRepository.getUserRef(user.uid)
          val associationRef = associationRepository.getAssociationRef(association.uid)

          batch.set(associationRef, AssociationRepositoryFirestore.serialize(association))
          batch.set(userRef, UserRepositoryFirestore.serialize(user))
        Log.d("ConcurrentAssociationUserRepositoryFirestore", "updateFollow: walked through ${user.firstName} ${association.name}")
        }
        .addOnSuccessListener {
            Log.d("ConcurrentAssociationUserRepositoryFirestore", "updateFollow: success")
            onSuccess() }
        .addOnFailureListener {
            Log.e("ConcurrentAssociationUserRepositoryFirestore", "updateFollow: failed")
            onFailure(it) }
  }
}
