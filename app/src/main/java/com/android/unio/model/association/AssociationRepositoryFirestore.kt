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
import com.google.firebase.firestore.firestore

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
                  val association = hydrate(document)

                  associations.add(association)
              }
              onSuccess(associations)
          },
          onFailure = { exception -> onFailure(exception) }
      )
  }

  override fun getAssociationWithId(
      id: String,
      onSuccess: (Association) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
      performFirestoreOperation(
          db.collection(ASSOCIATION_PATH).document(id).get(),
          onSuccess = { document -> onSuccess(hydrate(document)) },
          onFailure = { exception -> onFailure(exception) }
      )
  }

  override fun addAssociation(
      association: Association,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    performFirestoreOperation(
        db.collection(ASSOCIATION_PATH).document(association.uid).set(association),
        onSuccess = { onSuccess() },
        onFailure)
  }

  override fun updateAssociation(
      association: Association,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    performFirestoreOperation(
        db.collection(ASSOCIATION_PATH).document(association.uid).set(association),
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

  companion object {
    fun hydrate(doc: DocumentSnapshot): Association {
      val memberUids = doc.get("members") as? List<String> ?: emptyList()
      val members =
          FirestoreReferenceList.fromList(
              list = memberUids,
              collection = Firebase.firestore.collection(USER_PATH),
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
