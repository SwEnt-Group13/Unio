package com.android.unio.model.user

import android.util.Log
import com.android.unio.model.firestore.FirestorePaths.USER_PATH
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
    Firebase.auth.addAuthStateListener {
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
    getUserRef(id)
        .get()
        .addOnSuccessListener { document ->
          val user = hydrate(document.data)
          onSuccess(user)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
      Log.d("AAA", "j'essaie d'update le user wesh")
    getUserRef(user.uid)
        .set(serialize(user))
        .addOnSuccessListener { document -> onSuccess() }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  // Note: the following line is needed to add external methods to the companion object
  companion object
}
