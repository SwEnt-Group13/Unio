package com.android.unio.model.user

import com.android.unio.model.firestore.FirestorePaths.USER_PATH
import com.android.unio.model.firestore.transform.hydrate
import com.android.unio.model.firestore.transform.serialize
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class UserRepositoryFirestore @Inject constructor(private val db: FirebaseFirestore) :
    UserRepository {

  override fun init(onSuccess: (String, Boolean) -> Unit) {
    Firebase.auth.addAuthStateListener {
      if (it.currentUser != null) {
        onSuccess(it.currentUser!!.uid, true)
      }
    }
  }

  override fun getUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(USER_PATH)
        .get()
        .addOnSuccessListener { result ->
          val user = result.map { hydrate(it.data) }
          onSuccess(user)
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

  override fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(USER_PATH)
        .document(user.uid)
        .set(serialize(user))
        .addOnSuccessListener { document -> onSuccess() }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  // Note: the following line is needed to add external methods to the companion object
  companion object
}
