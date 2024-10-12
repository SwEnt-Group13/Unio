package com.android.unio.model.user

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

fun signInOrCreateAccount(
    email: String,
    password: String,
    auth: FirebaseAuth,
    onResult: (SignInResult) -> Unit,
) {
  if (isValidEmail(email)) {
    auth
        .signInWithEmailAndPassword(email, password)
        .addOnSuccessListener { onResult(SignInResult(SignInState.SUCCESS_SIGN_IN, it.user)) }
        .addOnFailureListener {
          if (it is FirebaseAuthInvalidCredentialsException) {
            auth
                .createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                  onResult(SignInResult(SignInState.SUCCESS_CREATE_ACCOUNT, it.user))
                }
                .addOnFailureListener {
                  onResult(SignInResult(SignInState.INVALID_CREDENTIALS, null))
                }
          } else {
            onResult(SignInResult(SignInState.INVALID_CREDENTIALS, null))
          }
        }
  } else {
    onResult(SignInResult(SignInState.INVALID_EMAIL_FORMAT, null))
  }
}

fun isValidEmail(text: String): Boolean {
  return text.trim().isNotEmpty() && text.trim().matches(Regex("^.+@.+\\..+$"))
}
