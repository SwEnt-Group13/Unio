package com.android.unio.model.follow

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

/**
 * A Firestore implementation of [ConcurrentAssociationUserRepository]. This class is responsible
 * for updating the Firestore database with the user's follow status for an association.
 *
 * @property db The Firestore database.
 * @property userRepository The repository for user data.
 * @property associationRepository The repository for association data.
 */
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

  /**
   * Updates the Firestore database with the user's follow status for an association. This operation
   * is performed atomically. If the operation fails, the database is not updated.
   *
   * @param user The user.
   * @param association The association.
   * @param onSuccess The callback that is called when the operation is successful.
   * @param onFailure The callback that is called when the operation fails.
   */
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
        }
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
  }
}
