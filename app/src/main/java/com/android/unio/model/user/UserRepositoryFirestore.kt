package com.android.unio.model.user

import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.firestore.FirestorePaths.ASSOCIATION_PATH
import com.android.unio.model.firestore.FirestorePaths.USER_PATH
import com.android.unio.model.firestore.FirestoreReferenceList
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class UserRepositoryFirestore(private val db: FirebaseFirestore) : UserRepository {

  override fun init(onSuccess: () -> Unit) {
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
          val associations = result.map { hydrate(it) }
          onSuccess(associations)
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
          val association = hydrate(document)
          onSuccess(association)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  companion object {
    fun hydrate(doc: DocumentSnapshot): User {
      val db = FirebaseFirestore.getInstance()

      val followingAssociationsUids =
          doc.get("followingAssociations") as? List<String> ?: emptyList()
      val followingAssociations =
          FirestoreReferenceList.fromList(
              followingAssociationsUids,
              db,
              ASSOCIATION_PATH,
              AssociationRepositoryFirestore::hydrate)

      return User(
          uid = doc.id,
          name = doc.getString("name") ?: "",
          email = doc.getString("email") ?: "",
          followingAssociations = followingAssociations)
    }
  }
}
