package com.android.unio.model.user

import com.android.unio.model.authentication.registerAuthStateListener
import com.android.unio.model.firestore.FirestorePaths.USER_PATH
import com.android.unio.model.firestore.performFirestoreOperation
import com.android.unio.model.firestore.registerSnapshotListener
import com.android.unio.model.firestore.transform.hydrate
import com.android.unio.model.firestore.transform.serialize
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import javax.inject.Inject

class UserRepositoryFirestore @Inject constructor(private val db: FirebaseFirestore) :
    UserRepository {

  override fun init(onSuccess: () -> Unit) {
    Firebase.auth.registerAuthStateListener {
      if (it.currentUser != null) {
        onSuccess()
      }
    }
  }

  override fun getUserRef(uid: String): DocumentReference {
    return db.collection(USER_PATH).document(uid)
  }

  override fun getUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(USER_PATH)
        .get()
        .performFirestoreOperation(
            onSuccess = { result ->
              val users = result.map { hydrate(it.data) }
              onSuccess(users)
            },
            onFailure = onFailure)
  }

  /**
   * Gets the user with the given id. Here, instead of using success and failure listener directly,
   * we use a Snapshot Listener that call directly the callback when a read/write are made on the
   * local (cache) database.
   *
   * @param id [String] : the id of the user to get.
   * @param onSuccess [(User) -> Unit] : the callback to call when the user is found.
   * @param onFailure [(Exception) -> Unit] : the callback to call when an error occurs.
   */
  override fun getUserWithId(
      id: String,
      onSuccess: (User) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    getUserRef(id).registerSnapshotListener(MetadataChanges.EXCLUDE) { documentSnapshot, exception
      ->
      if (exception != null) {
        onFailure(exception)
        return@registerSnapshotListener
      }

      if (documentSnapshot != null && documentSnapshot.exists()) {
        val user = hydrate(documentSnapshot.data)
        onSuccess(user)
      }
    }
  }

  override fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    getUserRef(user.uid)
        .set(serialize(user))
        .performFirestoreOperation(onSuccess = { onSuccess() }, onFailure = onFailure)
  }

  override fun deleteUserInFirestore(
      userId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(USER_PATH)
        .document(userId)
        .delete()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
  }

  override fun deleteUserInAuth(
      userId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val currentUser = Firebase.auth.currentUser

    if (currentUser!!.uid != userId) {
      onFailure(Exception("User is not authenticated"))
      return
    }

    currentUser.delete().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        onSuccess()
      } else {
        onFailure(task.exception!!)
      }
    }
  }

  // Note: the following line is needed to add external methods to the companion object
  companion object
}
