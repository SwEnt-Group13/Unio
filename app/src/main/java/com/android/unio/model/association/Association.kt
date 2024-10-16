package com.android.unio.model.association

import com.android.unio.model.firestore.FirestorePaths.ASSOCIATION_PATH
import com.android.unio.model.firestore.FirestoreReferenceList
import com.android.unio.model.firestore.ReferenceList
import com.android.unio.model.firestore.transform.hydrate
import com.android.unio.model.user.User

/**
 * Association data class Make sure to update the hydration and serialization methods when changing
 * the data class
 *
 * @property uid association id
 * @property url association url
 * @property acronym association acronym
 * @property fullName association full name
 * @property description association description
 * @property members list of association members
 */
data class Association(
    val uid: String,
    val url: String = "",
    val acronym: String = "",
    val fullName: String = "",
    val description: String = "",
    val members: ReferenceList<User>
) {
  companion object {
    fun emptyFirestoreReferenceList(): FirestoreReferenceList<Association> {
      return FirestoreReferenceList.empty(
          collectionPath = ASSOCIATION_PATH,
          hydrate = AssociationRepositoryFirestore.Companion::hydrate)
    }

    fun firestoreReferenceListWith(uids: List<String>): FirestoreReferenceList<Association> {
      return FirestoreReferenceList.fromList(
          list = uids,
          collectionPath = ASSOCIATION_PATH,
          hydrate = AssociationRepositoryFirestore.Companion::hydrate)
    }
  }
}
