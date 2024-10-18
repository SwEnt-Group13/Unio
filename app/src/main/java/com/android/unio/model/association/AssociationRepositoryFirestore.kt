package com.android.unio.model.association

import android.util.Log
import com.android.unio.model.firestore.FirestorePaths.ASSOCIATION_PATH
import com.android.unio.model.firestore.transform.hydrate
import com.android.unio.model.firestore.transform.serialize
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
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
    performFirestoreOperation(
        db.collection(ASSOCIATION_PATH).get(),
        onSuccess = { result ->
          val associations = mutableListOf<Association>()
          for (document in result) {
            val association = hydrate(document.data)

            associations.add(association)
          }
          onSuccess(associations)
        },
        onFailure = { exception -> onFailure(exception) })
  }

  override fun getAssociationsByCategory(
      category: AssociationCategory,
      onSuccess: (List<Association>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    performFirestoreOperation(
        db.collection(ASSOCIATION_PATH).whereEqualTo("category", category).get(),
        onSuccess = { result ->
          val associations = mutableListOf<Association>()
          for (document in result) {
            val association = hydrate(document.data)

            associations.add(association)
          }
          onSuccess(associations)
        },
        { exception -> onFailure(exception) })
  }

  override fun getAssociationWithId(
      id: String,
      onSuccess: (Association) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    performFirestoreOperation(
        db.collection(ASSOCIATION_PATH).document(id).get(),
        onSuccess = { document -> onSuccess(hydrate(document.data)) },
        onFailure = { exception -> onFailure(exception) })
  }

  override fun addAssociation(
      association: Association,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    performFirestoreOperation(
        db.collection(ASSOCIATION_PATH).document(association.uid).set(serialize(association)),
        onSuccess = { onSuccess() },
        onFailure)
  }

  override fun updateAssociation(
      association: Association,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    performFirestoreOperation(
        db.collection(ASSOCIATION_PATH).document(association.uid).set(serialize(association)),
        onSuccess = { onSuccess() },
        onFailure)
  }

  override fun deleteAssociationById(
      associationId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    performFirestoreOperation(
        db.collection(ASSOCIATION_PATH).document(associationId).delete(),
        onSuccess = { onSuccess() },
        onFailure)
  }

  /** Performs a Firestore operation and calls the appropriate callback based on the result. */
  private fun <T> performFirestoreOperation(
      task: Task<T>,
      onSuccess: (T) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    task.addOnCompleteListener {
      if (it.isSuccessful) {
        it.result?.let { result -> onSuccess(result) }
      } else {
        it.exception?.let { e ->
          Log.e("AssociationRepositoryFirestore", "Error performing Firestore operation", e)
          onFailure(e)
        }
      }
    }
  }

  // Note: the following line is needed to add external methods to the companion object
  companion object
}
