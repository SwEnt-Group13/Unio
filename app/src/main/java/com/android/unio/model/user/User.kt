package com.android.unio.model.user

import com.android.unio.model.association.Association
import com.android.unio.model.firestore.FirestoreReferenceList

data class User(
    val id: String,
    val name: String,
    val email: String,
    val followingAssociations: FirestoreReferenceList<Association>
)
