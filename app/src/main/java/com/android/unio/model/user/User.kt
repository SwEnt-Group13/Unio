package com.android.unio.model.user

import com.android.unio.model.association.Association
import com.android.unio.model.firestore.FirestorePaths.USER_PATH
import com.android.unio.model.firestore.FirestoreReferenceList
import com.android.unio.model.firestore.ReferenceList
import com.android.unio.model.firestore.transform.hydrate
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * User data class Make sure to update the hydration and serialization methods when changing the
 * data class
 *
 * @property uid user id
 * @property name user name
 * @property email user email
 * @property followingAssociations list of associations that the user is following
 */
data class User(
    val uid: String,
    val name: String,
    val email: String,
    val followingAssociations: ReferenceList<Association>
) {
  companion object {
    fun emptyFirestoreReferenceList(): FirestoreReferenceList<User> {
      return FirestoreReferenceList.empty(
          Firebase.firestore.collection(USER_PATH), UserRepositoryFirestore.Companion::hydrate)
    }

    fun firestoreReferenceListWith(uids: List<String>): FirestoreReferenceList<User> {
      return FirestoreReferenceList.fromList(
          uids,
          Firebase.firestore.collection(USER_PATH),
          UserRepositoryFirestore.Companion::hydrate)
    }
  }
}
