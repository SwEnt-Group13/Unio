package com.android.unio.model.association

import android.util.Log
import com.android.unio.model.firestore.FirestorePaths.ASSOCIATION_PATH
import com.android.unio.model.firestore.FirestorePaths.USER_PATH
import com.android.unio.model.firestore.FirestoreReferenceList
import com.android.unio.model.user.UserRepositoryFirestore
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class AssociationRepositoryFirestore(private val db: FirebaseFirestore) : AssociationRepository {

  override fun init(onSuccess: () -> Unit) {
    Firebase.auth.addAuthStateListener {
      if (it.currentUser != null) {
        onSuccess()
      }
    }
  }

  override fun getAssociations(
      onSuccess: (List<Association>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(ASSOCIATION_PATH)
        .get()
        .addOnSuccessListener { result ->
          val associations = mutableListOf<Association>()
          for (document in result) {
            val association = hydrate(document)

            associations.add(association)
          }
          onSuccess(associations)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun getAssociationWithId(
      id: String,
      onSuccess: (Association) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(ASSOCIATION_PATH)
        .document(id)
        .get()
        .addOnSuccessListener { document ->
          val association = hydrate(document)
          onSuccess(association)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun addAssociation(
      association: Association,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    performFirestoreOperation(
        db.collection(ASSOCIATION_PATH).document(association.uid).set(association),
        onSuccess,
        onFailure)
  }

  override fun updateAssociation(
      association: Association,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    performFirestoreOperation(
        db.collection(ASSOCIATION_PATH).document(association.uid).set(association),
        onSuccess,
        onFailure)
  }

  override fun deleteAssociationById(
      associationId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    performFirestoreOperation(
        db.collection(ASSOCIATION_PATH).document(associationId).delete(), onSuccess, onFailure)
  }

  /** Performs a Firestore operation and calls the appropriate callback based on the result. */
  private fun performFirestoreOperation(
      task: Task<Void>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    task.addOnCompleteListener {
      if (it.isSuccessful) {
        onSuccess()
      } else {
        it.exception?.let { e ->
          Log.e("AssociationRepositoryFirestore", "Error performing Firestore operation", e)
          onFailure(e)
        }
      }
    }
  }

  companion object {
    fun hydrate(doc: DocumentSnapshot): Association {
      val memberUids = doc.get("members") as? List<String> ?: emptyList()
      val members =
          FirestoreReferenceList.fromList(
              list = memberUids,
              collectionPath = USER_PATH,
              hydrate = UserRepositoryFirestore::hydrate)

      return Association(
          uid = doc.id,
          url = doc.getString("url") ?: "",
          acronym = doc.getString("acronym") ?: "",
          fullName = doc.getString("fullName") ?: "",
          description = doc.getString("description") ?: "",
          members = members)
    }
  }
}
