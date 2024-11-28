package com.android.unio.model.user

import com.android.unio.model.authentication.registerAuthStateListener
import com.android.unio.model.firestore.FirestorePaths.USER_PATH
import com.android.unio.model.firestore.performFirestoreOperation
import com.android.unio.model.firestore.transform.hydrate
import com.android.unio.model.firestore.transform.serialize
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
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

  override fun getUserWithId(
      id: String,
      onSuccess: (User) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    getUserRef(id)
        .get()
        .performFirestoreOperation(
            onSuccess = { document ->
              val user = hydrate(document.data)
              onSuccess(user)
            },
            onFailure = onFailure)
  }

  override fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    getUserRef(user.uid)
        .set(serialize(user))
        .performFirestoreOperation(onSuccess = { onSuccess() }, onFailure = onFailure)
  }

    override fun deleteUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(USER_PATH).document(user.uid)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

  // Note: the following line is needed to add external methods to the companion object
  companion object
}
