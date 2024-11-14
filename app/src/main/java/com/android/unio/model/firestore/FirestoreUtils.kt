package com.android.unio.model.firestore

import android.util.Log
import com.google.android.gms.tasks.Task

/**
 * Extension function that performs a Firestore operation and calls the appropriate callback based
 * on the result.
 */
fun <T> Task<T>.performFirestoreOperation(onSuccess: (T) -> Unit, onFailure: (Exception) -> Unit) {
  this.addOnSuccessListener { result ->
        if (result == null) {
          Log.e("FirestoreUtils", "Error performing Firestore operation: result is null")
          onFailure(NullPointerException("Result is null"))
          return@addOnSuccessListener
        }
        onSuccess(result)
      }
      .addOnFailureListener { exception ->
        Log.e("FirestoreUtils", "Error performing Firestore operation", exception)
        onFailure(exception)
      }
}
