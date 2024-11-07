package com.android.unio.model.association

import android.util.Log
import com.android.unio.model.firestore.FirestorePaths.ASSOCIATION_PATH
import com.android.unio.model.firestore.transform.hydrate
import com.android.unio.model.firestore.transform.serialize
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class AssociationRepositoryFirestore @Inject constructor(private val db: FirebaseFirestore) :
    AssociationRepository {

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

  override fun saveAssociation(
      association: Association,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    Log.d("AssociationViewModel", "Association saved INSIDE repository")
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

  // TODO extract this to a util
  /** Performs a Firestore operation and calls the appropriate callback based on the result. */
  private fun <T> performFirestoreOperation(
      task: Task<T>,
      onSuccess: (T) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    Log.d("AssociationRepositoryFirestore", "Attempting Firestore operation.")
    task.addOnCompleteListener { taskResult ->
      if (taskResult.isSuccessful) {
        val result = taskResult.result
        if (result != null) {
          Log.d("AssociationRepositoryFirestore", "Firestore operation succeeded with result.")
          onSuccess(result)
        } else {
          Log.d(
              "AssociationRepositoryFirestore", "Firestore operation succeeded but result is null.")
          onSuccess(result)
        }
      } else {
        val exception = taskResult.exception
        exception?.let {
          Log.e("AssociationRepositoryFirestore", "Error performing Firestore operation", it)
          onFailure(it)
        }
      }
    }
  }

  companion object
}
