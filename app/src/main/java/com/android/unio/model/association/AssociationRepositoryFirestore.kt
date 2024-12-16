package com.android.unio.model.association

import com.android.unio.model.authentication.registerAuthStateListener
import com.android.unio.model.firestore.FirestorePaths.ASSOCIATION_PATH
import com.android.unio.model.firestore.FirestorePaths.ASSOCIATION_REQUEST_PATH
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

/**
 * Firestore implementation of the [AssociationRepository] interface.
 *
 * @property db [FirebaseFirestore] : The Firestore database instance. Injected by Hilt.
 */
class AssociationRepositoryFirestore @Inject constructor(private val db: FirebaseFirestore) :
    AssociationRepository {

  override fun init(onSuccess: () -> Unit) {
    Firebase.auth.registerAuthStateListener {
      if (it.currentUser != null && it.currentUser!!.isEmailVerified) {
        onSuccess()
      }
    }
  }

  /**
   * Fetches a [DocumentReference] for an [Association] object using the provided [uid].
   *
   * @param uid [String] : The uid of the [Association] to fetch.
   * @param isNewAssociation [Boolean] : The boolean that explains if the Association is newly
   *   created or not
   * @return [DocumentReference] : The [DocumentReference] for the [Association] object.
   */
  override fun getAssociationRef(uid: String, isNewAssociation: Boolean): DocumentReference {
    if (isNewAssociation) {
      return db.collection(ASSOCIATION_REQUEST_PATH).document(uid)
    } else {
      return db.collection(ASSOCIATION_PATH).document(uid)
    }
  }

  /**
   * Fetches all [Association] objects from Firestore.
   *
   * @param onSuccess [(List<Association>) -> Unit] : The callback to call when the [Association]s
   *   are fetched.
   * @param onFailure [(Exception) -> Unit] : The callback to call when the fetch fails.
   */
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

  /**
   * Fetches all [Association] objects from Firestore that belong to the provided [category].
   *
   * @param category [AssociationCategory] : The category of the [Association]s to fetch.
   * @param onSuccess [(List<Association>) -> Unit] : The callback to call when the [Association]s
   *   are fetched.
   * @param onFailure [(Exception) -> Unit] : The callback to call when the fetch fails.
   */
  fun getAssociationsByCategory(
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
   * Fetches an [Association] object from Firestore using the provided [id].
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
    db.collection(ASSOCIATION_PATH)
        .document(id)
        .get()
        .performFirestoreOperation(
            onSuccess = { result ->
              val association = hydrate(result.data)
              onSuccess(association)
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
  override fun registerAssociationListener(
      id: String,
      onSuccess: (Association) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    getAssociationRef(id, isNewAssociation = false).registerSnapshotListener(
        MetadataChanges.EXCLUDE) { documentSnapshot, exception ->
          if (exception != null) {
            onFailure(exception)
            return@registerSnapshotListener
          }
          if (documentSnapshot != null && documentSnapshot.exists()) {
            onSuccess(hydrate(documentSnapshot.data))
          }
        }
  }

  /**
   * Saves an [Association] object to Firestore.
   *
   * @param association [Association] : The [Association] object to save.
   * @param isNewAssociation [Boolean] : The boolean that explains if the Association is newly
   *   created or not
   * @param onSuccess [() -> Unit] : The callback to call when the [Association] is saved.
   * @param onFailure [(Exception) -> Unit] : The callback to call when the save fails.
   */
  override fun saveAssociation(
      isNewAssociation: Boolean,
      association: Association,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    getAssociationRef(association.uid, isNewAssociation)
        .set(serialize(association))
        .performFirestoreOperation(onSuccess = { onSuccess() }, onFailure = onFailure)
  }

  /**
   * Deletes an [Association] object from Firestore using the provided [associationId].
   *
   * @param associationId [String] : The id of the [Association] to delete.
   * @param onSuccess [() -> Unit] : The callback to call when the [Association] is deleted.
   * @param onFailure [(Exception) -> Unit] : The callback to call when the delete fails.
   */
  override fun deleteAssociationById(
      associationId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    getAssociationRef(associationId, isNewAssociation = false)
        .delete()
        .performFirestoreOperation(onSuccess = { onSuccess() }, onFailure = onFailure)
  }

  /**
   * Companion object for hydrating [Association] objects from Firestore documents. DO NOT REMOVE
   * THIS OBJECT.
   */
  companion object
}
