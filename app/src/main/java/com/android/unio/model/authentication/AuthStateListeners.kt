package com.android.unio.model.authentication

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener

private val authStateListeners = mutableListOf<AuthStateListener>()

/**
 * Registers an [AuthStateListener] with the [FirebaseAuth] instance. Registering the listener this
 * way allows it to be tracked and unregistered later without having its reference.
 *
 * @param callback The [AuthStateListener] to register.
 */
fun FirebaseAuth.registerAuthStateListener(callback: AuthStateListener) {
  authStateListeners.add(callback)
  this.addAuthStateListener(callback)
}

/** Unregisters all [AuthStateListener]s from the [FirebaseAuth] instance. */
fun FirebaseAuth.unregisterAllAuthStateListeners() {
  authStateListeners.forEach { this.removeAuthStateListener(it) }
  authStateListeners.clear()
}

/**
 * Returns the number of [AuthStateListener]s currently registered with the [FirebaseAuth] instance.
 */
fun FirebaseAuth.currentAuthStateListenerCount(): Int = authStateListeners.size
