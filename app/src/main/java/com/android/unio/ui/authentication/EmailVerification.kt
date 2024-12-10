package com.android.unio.ui.authentication

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.unio.R
import com.android.unio.model.authentication.AuthViewModel
import com.android.unio.model.strings.test_tags.EmailVerificationTestTags
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.theme.AppTypography
import com.android.unio.ui.utils.ToastUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

/**
 * A screen that allows users to verify their email.
 *
 * @param navigationAction The navigation action to use.
 * @param userViewModel The [UserViewModel] to use.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailVerificationScreen(navigationAction: NavigationAction, authViewModel: AuthViewModel) {

  val user by remember { mutableStateOf(Firebase.auth.currentUser) }
  if (user == null) {
    Log.e("EmailVerificationScreen", "User is null")
    return
  }

  var success by remember { mutableStateOf(false) }

  val context = LocalContext.current

  val checkEmailVerification = {
      Log.d("NavigationAuthScreen", "here are my credentials :) : ${authViewModel.credential.toString()}")

    Firebase.auth.currentUser?.reload()?.addOnCompleteListener {
      if (it.isSuccessful) {
        if (Firebase.auth.currentUser?.isEmailVerified == true) {
          if (authViewModel.credential == null) {
            Log.e("EmailVerificationScreen", "Credential is null")
          } else {
            Firebase.auth.currentUser
                ?.reauthenticate(authViewModel.credential!!)
                ?.addOnSuccessListener {
                  success = true
                    authViewModel.setCredential(null)
                }
          }
        }
      } else {
        Log.e("EmailVerificationScreen", "Failed to refresh", it.exception)
        ToastUtils.showToast(context, context.getString(R.string.email_verification_refresh_failed))
      }
    }
  }

  val resendEmail = {
    Firebase.auth.currentUser?.sendEmailVerification()?.addOnCompleteListener {
      if (it.isSuccessful) {
        ToastUtils.showToast(
            context, context.getString(R.string.email_verification_toast_email_sent))
      } else {
        Log.e("EmailVerificationScreen", "Failed to send email", it.exception)
        ToastUtils.showToast(context, context.getString(R.string.email_verification_sent_failed))
      }
    }
  }

  Scaffold(
      modifier = Modifier.testTag(EmailVerificationTestTags.SCREEN).fillMaxSize(),
      topBar = {
        TopAppBar(
            title = {},
            navigationIcon = {
              IconButton(onClick = { Firebase.auth.signOut() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription =
                        context.getString(R.string.email_verification_content_description_go_back))
              }
            })
      },
      content = { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally) {
              if (success) {
                Text(
                    context.getString(R.string.email_verification_verified),
                    style = AppTypography.titleLarge)
                Button(
                    modifier = Modifier.testTag(EmailVerificationTestTags.CONTINUE),
                    onClick = { navigationAction.navigateTo(Screen.ACCOUNT_DETAILS) },
                ) {
                  Text(context.getString(R.string.email_verification_verified_continue))
                }
              } else {
                Text(
                    context.getString(R.string.email_verification_verify_request),
                    style = AppTypography.titleLarge)
                Text(
                    context.getString(R.string.email_verification_email_sent_to) +
                        " " +
                        user!!.email +
                        context.getString(R.string.email_verification_email_sent_to_verify),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(0.8f),
                    style = AppTypography.bodyMedium)
                OutlinedButton(
                    onClick = { resendEmail() },
                ) {
                  Text(
                      context.getString(R.string.email_verification_email_resend),
                      style = AppTypography.labelLarge)
                }

                Button(
                    modifier = Modifier.testTag(EmailVerificationTestTags.REFRESH),
                    onClick = { checkEmailVerification() }) {
                      Icon(
                          Icons.Outlined.Refresh,
                          contentDescription =
                              context.getString(
                                  R.string.email_verification_content_description_refresh))
                      Spacer(Modifier.width(8.dp))
                      Text(
                          context.getString(R.string.email_verification_email_refresh),
                          style = AppTypography.labelLarge)
                    }
              }
            }
      })
}
