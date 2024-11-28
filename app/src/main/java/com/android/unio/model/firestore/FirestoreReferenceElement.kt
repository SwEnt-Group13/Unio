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

    private var _uid: String = ""

    override val uid: String
        get() = _uid

    override fun set(uid: String) {
        _uid = uid
    }

    private val _element = MutableStateFlow<T?>(null)
    override val element: StateFlow<T?> = _element


    override fun fetch(onSuccess: () -> Unit, lazy: Boolean) {
        if (lazy && _element.value?.uid == _uid) {
            onSuccess()
            return
        }

        _element.value = null

        if (_uid.isEmpty()) {
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
                    onSuccess()
                }
                .addOnFailureListener { exception ->
                    Log.e("FirestoreReferenceElement", "Failed to fetch document", exception)
                }
        } else {
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

        /** Creates a [FirestoreReferenceElement] for a specific UID. */
        fun <T : UniquelyIdentifiable> withUid(
            uid: String,
            collectionPath: String? = null,
            hydrate: (Map<String, Any>?) -> T
        ): FirestoreReferenceElement<T> {
            return FirestoreReferenceElement(collectionPath, hydrate).apply {
                set(uid)
            }
        }
    }
}

fun User.Companion.emptyFirestoreReferenceElement(): FirestoreReferenceElement<User> {
    return FirestoreReferenceElement.empty(
        collectionPath = FirestorePaths.USER_PATH,
        hydrate = UserRepositoryFirestore.Companion::hydrate
    )
}

fun User.Companion.firestoreReferenceElementWith(uid: String): FirestoreReferenceElement<User> {
    return FirestoreReferenceElement.withUid(
        uid = uid,
        collectionPath = FirestorePaths.USER_PATH,
        hydrate = UserRepositoryFirestore.Companion::hydrate
    )
}