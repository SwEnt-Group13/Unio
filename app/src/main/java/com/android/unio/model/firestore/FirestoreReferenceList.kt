package com.android.unio.model.firestore

import android.util.Log
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.event.EventUserPicture
import com.android.unio.model.event.EventUserPictureRepositoryFirestore
import com.android.unio.model.firestore.FirestorePaths.ASSOCIATION_PATH
import com.android.unio.model.firestore.FirestorePaths.EVENT_PATH
import com.android.unio.model.firestore.FirestorePaths.EVENT_USER_PICTURES_PATH
import com.android.unio.model.firestore.FirestorePaths.USER_PATH
import com.android.unio.model.firestore.transform.hydrate
import com.android.unio.model.user.User
import com.android.unio.model.user.UserRepositoryFirestore
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
class FirestoreReferenceList<T : UniquelyIdentifiable>(
    private val collectionPath: String,
    private val hydrate: (Map<String, Any>?) -> T
) : ReferenceList<T> {
  // The internal list of UIDs.
  private val _uids = mutableListOf<String>()

  // The public list of UIDs.
  override val uids: List<String>
    get() = _uids

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
   * Updates an element in the list. If the element is not already present, it is added.
   *
   * @param element The element to update.
   */
  override fun update(element: T) {
    val index = _list.value.indexOfFirst { it.uid == element.uid }
    if (index != -1) {
      // Element exists; replace it
      _list.value = _list.value.toMutableList().apply { this[index] = element }
    } else {
      // Element does not exist; add it
      add(element)
    }
  }

  /**
   * Adds an element to the list.
   *
   * @param element The element to add.
   */
  override fun add(element: T) {
    add(element.uid)
    _list.value += element
  }

  /**
   * Adds a list of UIDs to the list.
   *
   * @param uids The UIDs to add.
   */
  override fun addAll(uids: List<String>) {
    _uids.addAll(uids)
  }

  /**
   * Removes a UID from the list.
   *
   * @param uid The UID to remove.
   */
  override fun remove(uid: String) {
    if (_uids.remove(uid)) {
      _list.value = _list.value.filter { it.uid != uid }
    }
  }

  /**
   * Checks if the list contains a UID.
   *
   * @param uid The UID to check.
   * @return True if the list contains the UID, false otherwise.
   */
  override fun contains(uid: String): Boolean {
    return _uids.contains(uid)
  }

  /**
   * Requests all documents from Firestore and updates the list.
   *
   * @param onSuccess A lambda that is called when the request is successful.
   * @param lazy If true, the request will only be made if the list is not up-to-date.
   */
  override fun requestAll(onSuccess: () -> Unit, lazy: Boolean) {
    // If the list is already up-to-date, return early.
    val fetchedUids = _list.value.map { it.uid }
    if (lazy && fetchedUids == _uids) {
      onSuccess()
      return
    }

    // If there are no UIDs, return early.
    if (_uids.isEmpty()) {
      _list.value = emptyList()
      onSuccess()
      return
    }

    Firebase.firestore
        .collection(collectionPath)
        .whereIn(FieldPath.documentId(), _uids.filter { it.isNotEmpty() })
        .get()
        .addOnSuccessListener { result ->
          val items = result.documents.map { hydrate(it.data) }
          _list.value = items

          onSuccess()
        }
        .addOnFailureListener { exception ->
          _list.value = emptyList()
          Log.e("FirestoreReferenceList", "Failed to get documents", exception)
        }
  }

  companion object {
    /** Creates a [FirestoreReferenceList] from a list of UIDs. */
    fun <T : UniquelyIdentifiable> fromList(
        list: List<String>,
        collectionPath: String,
        hydrate: (Map<String, Any>?) -> T
    ): FirestoreReferenceList<T> {
      val result = FirestoreReferenceList(collectionPath, hydrate)
      result.addAll(list)
      return result
    }

    /** Creates an empty [FirestoreReferenceList]. */
    fun <T : UniquelyIdentifiable> empty(
        collectionPath: String,
        hydrate: (Map<String, Any>?) -> T
    ): FirestoreReferenceList<T> {
      return FirestoreReferenceList(collectionPath, hydrate)
    }
  }
}

/**
 * Extension function for creating an empty [FirestoreReferenceList] objects for [Association].
 *
 * @return The [FirestoreReferenceList] object.
 */
fun Association.Companion.emptyFirestoreReferenceList(): FirestoreReferenceList<Association> {
  return FirestoreReferenceList.empty(
      ASSOCIATION_PATH, AssociationRepositoryFirestore.Companion::hydrate)
}

/**
 * Extension function for creating a [FirestoreReferenceList] objects for [Association] with a list
 * of UIDs.
 *
 * @param uids The list of UIDs.
 * @return The [FirestoreReferenceList] object.
 */
fun Association.Companion.firestoreReferenceListWith(
    uids: List<String>
): FirestoreReferenceList<Association> {
  return FirestoreReferenceList.fromList(
      uids, ASSOCIATION_PATH, AssociationRepositoryFirestore.Companion::hydrate)
}

/**
 * Extension function for creating an empty [FirestoreReferenceList] objects for [User].
 *
 * @return The [FirestoreReferenceList] object.
 */
fun User.Companion.emptyFirestoreReferenceList(): FirestoreReferenceList<User> {
  return FirestoreReferenceList.empty(USER_PATH, UserRepositoryFirestore.Companion::hydrate)
}

/**
 * Extension function for creating a [FirestoreReferenceList] objects for [User] with a list of
 * UIDs.
 *
 * @param uids The list of UIDs.
 * @return The [FirestoreReferenceList] object.
 */
fun User.Companion.firestoreReferenceListWith(uids: List<String>): FirestoreReferenceList<User> {
  return FirestoreReferenceList.fromList(
      uids, USER_PATH, UserRepositoryFirestore.Companion::hydrate)
}

/**
 * Extension function for creating an empty [FirestoreReferenceList] objects for [Event].
 *
 * @return The [FirestoreReferenceList] object.
 */
fun Event.Companion.emptyFirestoreReferenceList(): FirestoreReferenceList<Event> {
  return FirestoreReferenceList.empty(EVENT_PATH, EventRepositoryFirestore.Companion::hydrate)
}

/**
 * Extension function for creating a [FirestoreReferenceList] objects for [Event] with a list of
 * UIDs.
 *
 * @param uids The list of UIDs.
 * @return The [FirestoreReferenceList] object.
 */
fun Event.Companion.firestoreReferenceListWith(uids: List<String>): FirestoreReferenceList<Event> {
  return FirestoreReferenceList.fromList(
      uids, EVENT_PATH, EventRepositoryFirestore.Companion::hydrate)
}

fun EventUserPicture.Companion.emptyFirestoreReferenceList():
    FirestoreReferenceList<EventUserPicture> {
  return FirestoreReferenceList.empty(
      EVENT_USER_PICTURES_PATH, EventUserPictureRepositoryFirestore.Companion::hydrate)
}

fun EventUserPicture.Companion.firestoreReferenceListWith(
    uids: List<String>
): FirestoreReferenceList<EventUserPicture> {
  return FirestoreReferenceList.fromList(
      uids, EVENT_USER_PICTURES_PATH, EventUserPictureRepositoryFirestore.Companion::hydrate)
}
