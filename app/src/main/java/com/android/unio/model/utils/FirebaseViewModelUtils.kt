package com.android.unio.model.utils

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

fun createFirebaseUid(collectionPath : String): String{
    return Firebase.firestore
        .collection(collectionPath)
        .document()
        .id // creates a new document reference and retrieve the randomly generated
}
