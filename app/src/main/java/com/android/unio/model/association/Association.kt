package com.android.unio.model.association

data class Association(
    val uid: String,
    val url: String = "",
    val acronym: String = "",
    val fullName: String = "",
    val description: String = "",
    val members: List<String> = emptyList()
)
