package com.android.unio.ui.authentication

import android.util.Log
import android.widget.Toast
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
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.theme.AppTypography
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailVerificationScreen(navigationAction: NavigationAction) {

  val user by remember { mutableStateOf(Firebase.auth.currentUser) }
  var success by remember { mutableStateOf(false) }

  val context = LocalContext.current

  val checkEmailVerification = {
    Firebase.auth.currentUser?.reload()?.addOnCompleteListener {
      if (it.isSuccessful) {
        if (Firebase.auth.currentUser?.isEmailVerified == true) {
          success = true
        }
      } else {
        Log.e("EmailVerificationScreen", "Failed to refresh", it.exception)
        Toast.makeText(context, "Failed to refresh", Toast.LENGTH_SHORT).show()
      }
    }
  }

  val resendEmail = {
    Firebase.auth.currentUser?.sendEmailVerification()?.addOnCompleteListener {
      if (it.isSuccessful) {
        Toast.makeText(context, "Email sent", Toast.LENGTH_SHORT).show()
      } else {
        Log.e("EmailVerificationScreen", "Failed to send email", it.exception)
        Toast.makeText(context, "Failed to send email", Toast.LENGTH_SHORT).show()
      }
    }
  }

  Scaffold(
      modifier = Modifier.testTag("EmailVerificationScreen").fillMaxSize(),
      topBar = {
        TopAppBar(
            title = {},
            navigationIcon = {
              IconButton(onClick = { Firebase.auth.signOut() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Go back")
              }
            })
      },
      content = { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally) {
              if (success) {
                Text("Email verified!", style = AppTypography.titleLarge)
                Button(
                    onClick = { navigationAction.navigateTo(Screen.ACCOUNT_DETAILS) },
                ) {
                  Text("Continue")
                }
              } else {
                Text("Verify your email", style = AppTypography.titleLarge)
                Text(
                    "We sent an email to ${user?.email ?: ""}. Please verify your email to continue.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(0.8f),
                    style = AppTypography.bodyMedium)
                OutlinedButton(
                    onClick = { resendEmail() },
                ) {
                  Text("Resend email", style = AppTypography.labelLarge)
                }

                Button(onClick = { checkEmailVerification() }) {
                  Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                  Spacer(Modifier.width(8.dp))
                  Text("Refresh", style = AppTypography.labelLarge)
                }
              }
            }
      })
}
