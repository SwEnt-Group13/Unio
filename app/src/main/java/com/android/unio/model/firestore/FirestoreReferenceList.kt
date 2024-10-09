package com.android.unio.model.firestore

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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
 * @property db The [FirebaseFirestore] instance to use.
 * @property collectionPath The path to the Firestore collection that contains the objects.
 * @property hydrate A function that converts a [DocumentSnapshot] to a [T].
 */
class FirestoreReferenceList<T>(
    private val db: FirebaseFirestore,
    private val collectionPath: String,
    private val hydrate: (DocumentSnapshot) -> T
) : ReferenceList {
  // The internal list of UIDs.
  private var _uids = mutableListOf<String>()

  // The internal list of objects.
  private val _list = MutableStateFlow<List<T>>(emptyList())

  // The public list of objects.
  val list: StateFlow<List<T>> = _list

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
    println("Requesting all")
    _list.value = emptyList()
    _uids.forEach { uid ->
      db.collection(collectionPath).document(uid).get().addOnSuccessListener { result ->
        val item = hydrate(result)
        _list.value += item
        println("Added $item")
      }
    }
  }

  companion object {
    /** Creates a [FirestoreReferenceList] from a list of UIDs. */
    fun <T> fromList(
        list: List<String>,
        db: FirebaseFirestore = Firebase.firestore,
        collectionPath: String,
        hydrate: (DocumentSnapshot) -> T
    ): FirestoreReferenceList<T> {
      val result = FirestoreReferenceList(db, collectionPath, hydrate)
      result.addAll(list)
      return result
    }

    /** Creates an empty [FirestoreReferenceList]. */
    fun <T> empty(
        db: FirebaseFirestore = Firebase.firestore,
        collectionPath: String,
        hydrate: (DocumentSnapshot) -> T
    ): FirestoreReferenceList<T> {
      return FirestoreReferenceList(db, collectionPath, hydrate)
    }
  }
}
