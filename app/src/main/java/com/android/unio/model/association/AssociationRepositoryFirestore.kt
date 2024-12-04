package com.android.unio.model.association

import com.android.unio.model.authentication.registerAuthStateListener
import com.android.unio.model.firestore.FirestorePaths.ASSOCIATION_PATH
import com.android.unio.model.firestore.performFirestoreOperation
import com.android.unio.model.firestore.registerSnapshotListener
import com.android.unio.model.firestore.transform.hydrate
import com.android.unio.model.firestore.transform.serialize
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.QuerySnapshot
import javax.inject.Inject

class AssociationRepositoryFirestore @Inject constructor(private val db: FirebaseFirestore) :
    AssociationRepository {

  override fun init(onSuccess: () -> Unit) {
    Firebase.auth.registerAuthStateListener {
      if (it.currentUser != null) {
        onSuccess()
      }
    }
  }

  override fun getAssociationRef(uid: String): DocumentReference {
    return db.collection(ASSOCIATION_PATH).document(uid)
  }

  override fun getAssociations(
      onSuccess: (List<Association>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(ASSOCIATION_PATH)
        .get()
        .performFirestoreOperation<QuerySnapshot>(
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
    db.collection(ASSOCIATION_PATH)
        .whereEqualTo("category", category)
        .get()
        .performFirestoreOperation(
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

  /**
   * Fetches an [Association] object from Firestore using the provided [id]. Here, instead of using
   * success and failure listener directly, we use a Snapshot Listener that call directly the
   * callback when a read/write are made on the local (cache) database.
   *
   * @param id [String] : The id of the [Association] to fetch.
   * @param onSuccess [(Association) -> Unit] : The callback to call when the [Association] is
   *   fetched.
   * @param onFailure [(Exception) -> Unit] : The callback to call when the fetch fails.
   */
  override fun getAssociationWithId(
      id: String,
      onSuccess: (Association) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    getAssociationRef(id).registerSnapshotListener(MetadataChanges.EXCLUDE) {
        documentSnapshot,
        exception ->
      if (exception != null) {
        onFailure(exception)
        return@registerSnapshotListener
      }
      if (documentSnapshot != null && documentSnapshot.exists()) {
        onSuccess(hydrate(documentSnapshot.data))
      }
    }
  }

  override fun saveAssociation(
      association: Association,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    getAssociationRef(association.uid)
        .set(serialize(association))
        .performFirestoreOperation(onSuccess = { onSuccess() }, onFailure = onFailure)
  }

  override fun deleteAssociationById(
      associationId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    getAssociationRef(associationId)
        .delete()
        .performFirestoreOperation(onSuccess = { onSuccess() }, onFailure = onFailure)
  }

  /**
   * Companion object for hydrating [Association] objects from Firestore documents. DO NOT REMOVE
   * THIS OBJECT.
   */
  companion object
}
