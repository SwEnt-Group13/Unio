package com.android.unio.model.user

import android.util.Log
import com.android.unio.model.authentication.AuthViewModel
import com.android.unio.model.image.ImageRepository
import com.android.unio.model.strings.StoragePathsStrings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

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
    auth.signInWithEmailAndPassword(email.trim(), password).addOnCompleteListener {
      if (it.isSuccessful) {
        onResult(SignInResult(SignInState.SUCCESS_SIGN_IN, it.result.user))
      } else {
        if (it.exception is FirebaseAuthInvalidUserException ||
            it.exception is FirebaseAuthInvalidCredentialsException) {
          createAccount(email.trim(), password, auth, onResult)
        } else {
          Log.e("Auth", "Failed to sign in", it.exception)
          onResult(SignInResult(SignInState.INVALID_CREDENTIALS, null))
        }
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

/**
 * Deletes the user with the given userId from firebase auth, storage (for the profile picture) and
 * firestore
 *
 * @param userId The Id of the corresponding user we want to delete
 * @param authViewModel The instance of the authViewModel to delete the user in firebase auth
 * @param userViewModel The instance of the userViewModel to delete the user in firestore
 * @param imageRepository The instance of the image repository to delete the user's profile picture
 * @return true if all three method were successful and false otherwise
 */
suspend fun deleteUser(
    userId: String,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    imageRepository: ImageRepository,
    deleteWithProfilePicture: Boolean,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
  try {
    coroutineScope {

      // Only delete the profile picture if the user has one!
      if (deleteWithProfilePicture) {
        val imageTask = async {
          imageRepository.deleteImage(
              StoragePathsStrings.USER_IMAGES + userId,
              onSuccess = { Log.i("UserDeletion", "Successfully deleted user's profile picture") },
              onFailure = { throw it })
        }
        imageTask.await()
      }

      val authTask = async {
        authViewModel.deleteAccount(
            userId,
            onSuccess = { Log.i("UserDeletion", "Successfully deleted user from auth") },
            onFailure = { throw it })
      }

      val firestoreTask = async {
        userViewModel.deleteUserDocument(
            userId,
            onSuccess = { Log.i("UserDeletion", "Successfully deleted user from firestore") },
            onFailure = { throw it })
      }

      authTask.await()
      firestoreTask.await()

      onSuccess()
    }
  } catch (e: Exception) {
    Log.e("UserDeletion", "Failed to delete user: ${e.message}", e)
    onFailure(e)
  }
}
