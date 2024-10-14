package com.android.unio.model.user

import com.android.unio.model.association.Association
import com.android.unio.model.firestore.ReferenceList

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
)
