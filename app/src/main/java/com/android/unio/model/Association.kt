package com.android.unio.model

data class Association(
    val uid: String,
    val acronym: String,
    val fullName: String,
    val description: String,
    val members: List<String>
)
