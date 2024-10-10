package com.android.unio.model.association

import com.android.unio.model.firestore.ReferenceList
import com.android.unio.model.user.User

data class Association(
    val uid: String,
    val url: String = "",
    val acronym: String = "",
    val fullName: String = "",
    val description: String = "",
    val members: ReferenceList<User>
)
