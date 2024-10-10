package com.android.unio.model.user

import com.android.unio.model.association.Association
import com.android.unio.model.firestore.ReferenceList

data class User(
    val uid: String,
    val name: String,
    val email: String,
    val followingAssociations: ReferenceList<Association>
)
