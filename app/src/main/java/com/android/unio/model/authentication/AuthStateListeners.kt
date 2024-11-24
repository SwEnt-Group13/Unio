package com.android.unio.model.authentication

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener

private val authStateListeners = mutableListOf<AuthStateListener>()

fun FirebaseAuth.registerAuthStateListener(callback: AuthStateListener) {
  authStateListeners.add(callback)
  this.addAuthStateListener(callback)
}

fun FirebaseAuth.unregisterAuthStateListener(callback: AuthStateListener) {
  authStateListeners.remove(callback)
  this.removeAuthStateListener(callback)
}

fun FirebaseAuth.unregisterAllAuthStateListeners() {
  authStateListeners.forEach { this.removeAuthStateListener(it) }
  authStateListeners.clear()
}

fun FirebaseAuth.currentAuthStateListenerCount(): Int = authStateListeners.size
