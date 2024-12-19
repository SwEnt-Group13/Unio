import android.util.Log
import com.android.unio.model.firestore.FirestorePaths
import com.android.unio.model.firestore.ReferenceElement
import com.android.unio.model.firestore.UniquelyIdentifiable
import com.android.unio.model.firestore.transform.hydrate
import com.android.unio.model.user.User
import com.android.unio.model.user.UserRepositoryFirestore
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FirestoreReferenceElement<T : UniquelyIdentifiable>(
    private val collectionPath: String?,
    private val hydrate: (Map<String, Any>?) -> T
) : ReferenceElement<T> {

  // We should consider rewriting this in an efficient manner
  private var _uid: String = ""

  override val uid: String
    get() = _uid

  override fun set(uid: String) {
    _uid = uid
  }

  private val _element = MutableStateFlow<T?>(null)
  override val element: StateFlow<T?> = _element

  /**
   * Fetches the Firestore document with the current UID and hydrates it into the element.
   *
   * @param onSuccess Callback to run after the fetch is successful.
   * @param lazy If true, the fetch will only be performed if the element is not already hydrated.
   */
  override fun fetch(onSuccess: () -> Unit, lazy: Boolean) {
    if (lazy && _element.value?.uid == _uid) {
      onSuccess()
      return
    }

    if (_uid.isEmpty()) {
      _element.value = null
      onSuccess()
      return
    }

    if (collectionPath != null) {
      // Standard hydration case
      FirebaseFirestore.getInstance()
          .collection(collectionPath)
          .document(_uid)
          .get()
          .addOnSuccessListener { document ->
              _element.value = hydrate(document.data)
              element.value?.let { Log.d("AssociationActionsMembers", "uid de l'element : " + it.uid) }
            onSuccess()
          }
          .addOnFailureListener { exception ->
            _element.value = null
            Log.e("FirestoreReferenceElement", "Failed to fetch document", exception)
          }
    } else {
      Log.e("AssociationActionsMembers", "Missing collection path")
        Log.e("FirestoreReferenceElement", "Missing collection path")
    }
  }

  companion object {
    /** Creates an empty [FirestoreReferenceElement]. */
    fun <T : UniquelyIdentifiable> empty(
        collectionPath: String? = null,
        hydrate: (Map<String, Any>?) -> T
    ): FirestoreReferenceElement<T> {
      return FirestoreReferenceElement(collectionPath, hydrate)
    }

    /**
     * Creates a [FirestoreReferenceElement] for a specific UID.
     *
     * @param uid UID of the element.
     * @param collectionPath Path to the Firestore collection.
     * @param hydrate Function to hydrate Firestore data into the element.
     * @return FirestoreReferenceElement with the specified UID.
     */
    fun <T : UniquelyIdentifiable> withUid(
        uid: String,
        collectionPath: String? = null,
        hydrate: (Map<String, Any>?) -> T
    ): FirestoreReferenceElement<T> {
      return FirestoreReferenceElement(collectionPath, hydrate).apply { set(uid) }
    }
  }
}

/**
 * Creates an empty [FirestoreReferenceElement] for [User].
 *
 * @return Empty FirestoreReferenceElement for User.
 */
fun User.Companion.emptyFirestoreReferenceElement(): FirestoreReferenceElement<User> {
  return FirestoreReferenceElement.empty(
      collectionPath = FirestorePaths.USER_PATH,
      hydrate = UserRepositoryFirestore.Companion::hydrate)
}

/**
 * Creates a [FirestoreReferenceElement] for [User] with a specific UID.
 *
 * @param uid UID of the User.
 * @return FirestoreReferenceElement for User with the specified UID.
 */
fun User.Companion.firestoreReferenceElementWith(uid: String): FirestoreReferenceElement<User> {
  return FirestoreReferenceElement.withUid(
      uid = uid,
      collectionPath = FirestorePaths.USER_PATH,
      hydrate = UserRepositoryFirestore.Companion::hydrate)
}
