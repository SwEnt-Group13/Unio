package com.android.unio.model.user

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser

enum class SignInState {
  INVALID_CREDENTIALS,
  INVALID_EMAIL_FORMAT,
  SUCCESS_SIGN_IN,
  SUCCESS_CREATE_ACCOUNT
}

data class SignInResult(val state: SignInState, val user: FirebaseUser?)

/**
 * Sign in or create an account with the given email and password. The function will first try to
 * sign in with the given email and password. If the sign in fails, the function will try to create
 * an account.
 *
 * @param email The email to sign in or create an account with.
 * @param password The password to sign in or create an account with.
 * @param auth The FirebaseAuth instance to use for signing in or creating an account.
 * @param onResult The callback to call when the sign in or account creation is complete.
 */
fun signInOrCreateAccount(
    email: String,
    password: String,
    auth: FirebaseAuth,
    onResult: (SignInResult) -> Unit,
) {
  if (isValidEmail(email)) {
    auth
        .signInWithEmailAndPassword(email.trim(), password)
        .addOnSuccessListener { onResult(SignInResult(SignInState.SUCCESS_SIGN_IN, it.user)) }
        .addOnFailureListener {
          if (it is FirebaseAuthInvalidCredentialsException) {
            createAccount(email.trim(), password, auth, onResult)
          } else {
            Log.e("Auth", "Failed to sign in", it)
            onResult(SignInResult(SignInState.INVALID_CREDENTIALS, null))
          }
        }
  } else {
    onResult(SignInResult(SignInState.INVALID_EMAIL_FORMAT, null))
  }
}

/**
 * Create an account with the given email and password.
 *
 * @param email The email to create an account with.
 * @param password The password to create an account with.
 * @param auth The FirebaseAuth instance to use for creating an account.
 * @param onResult The callback to call when the account creation is complete.
 */
fun createAccount(
    email: String,
    password: String,
    auth: FirebaseAuth,
    onResult: (SignInResult) -> Unit
) {
  if (isValidEmail(email)) {
    auth
        .createUserWithEmailAndPassword(email.trim(), password)
        .addOnSuccessListener {
          onResult(SignInResult(SignInState.SUCCESS_CREATE_ACCOUNT, it.user))
        }
        .addOnFailureListener {
          Log.e("Auth", "Failed to create account", it)
          onResult(SignInResult(SignInState.INVALID_CREDENTIALS, null))
        }
  } else {
    onResult(SignInResult(SignInState.INVALID_EMAIL_FORMAT, null))
  }
}

/**
 * Check if the given text is a valid email.
 *
 * @param text The text to check.
 * @return true if the text is a valid email, false otherwise.
 */
fun isValidEmail(text: String): Boolean {
  return text.trim().let { it.isNotEmpty() && it.matches(Regex("^.+@.+\\..+$")) }
}

/**
 * Check if the given text is a valid password.
 *
 * @param text The text to check.
 * @return true if the password meets the requirements, false otherwise.
 */
fun isValidPassword(text: String): Boolean {
  return text.length in 6..4096 && text.contains(Regex("[0-9]"))
}
