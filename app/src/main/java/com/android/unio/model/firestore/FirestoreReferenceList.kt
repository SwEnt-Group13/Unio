package com.android.unio.model.firestore

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * A class that represents a list of Firestore objects that are identified by their UIDs. The list
 * is stored as a [StateFlow] and can be updated by calling [requestAll].
 *
 * In a @Composable function, the list should be accessed using:
 * ```kotlin
 * val list by FirestoreReferenceList.list.collectAsState()
 * ```
 *
 * @param T The type of the objects in the list.
 * @property collectionPath The path to the Firestore collection.
 * @property hydrate A function that converts a [DocumentSnapshot] to a [T].
 */
class FirestoreReferenceList<T>(
    private val collectionPath: String,
    private val hydrate: (Map<String, Any>?) -> T
) : ReferenceList<T> {
  // The internal list of UIDs.
  private val _uids = mutableListOf<String>()

  // The internal list of objects.
  private val _list = MutableStateFlow<List<T>>(emptyList())

  // The public list of objects.
  override val list: StateFlow<List<T>> = _list

  /**
   * Adds a UID to the list.
   *
   * @param uid The UID to add.
   */
  override fun add(uid: String) {
    _uids.add(uid)
  }

  /**
   * Adds a list of UIDs to the list.
   *
   * @param uids The UIDs to add.
   */
  override fun addAll(uids: List<String>) {
    _uids.addAll(uids)
  }

  /** Requests all documents from Firestore and updates the list. */
  override fun requestAll() {
    _list.value = emptyList()
    Firebase.firestore
        .collection(collectionPath)
        .whereIn(FieldPath.documentId(), _uids.filter { it.isNotEmpty() })
        .get()
        .addOnSuccessListener { result ->
          val items = result.documents.map { hydrate(it.data) }
          _list.value = items
        }
        .addOnFailureListener { exception ->
          Log.e("FirestoreReferenceList", "Failed to get documents", exception)
        }
  }

  companion object {
    /** Creates a [FirestoreReferenceList] from a list of UIDs. */
    fun <T> fromList(
        list: List<String>,
        collectionPath: String,
        hydrate: (Map<String, Any>?) -> T
    ): FirestoreReferenceList<T> {
      val result = FirestoreReferenceList(collectionPath, hydrate)
      result.addAll(list)
      return result
    }

    /** Creates an empty [FirestoreReferenceList]. */
    fun <T> empty(
        collectionPath: String,
        hydrate: (Map<String, Any>?) -> T
    ): FirestoreReferenceList<T> {
      return FirestoreReferenceList(collectionPath, hydrate)
    }
  }
}
