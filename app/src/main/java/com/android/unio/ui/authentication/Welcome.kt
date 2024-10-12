package com.android.unio.ui.authentication

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.unit.dp
import com.android.unio.model.user.SignInState
import com.android.unio.model.user.isValidEmail
import com.android.unio.model.user.signInOrCreateAccount
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.theme.AppTypography
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun WelcomeScreen(navigationAction: NavigationAction) {
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }

  var showPassword by remember { mutableStateOf(false) }

  val context = LocalContext.current

  val signIn = {
    signInOrCreateAccount(email, password, Firebase.auth) {
      // No need to navigate to other screens, that is already handled by the MainActivity
      when (it.state) {
        SignInState.INVALID_CREDENTIALS -> {
          Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
        }
        SignInState.INVALID_EMAIL_FORMAT -> {
          Toast.makeText(context, "Please enter a valid EPFL email address", Toast.LENGTH_SHORT)
              .show()
        }
        SignInState.SUCCESS_SIGN_IN -> {
          Toast.makeText(context, "Signed in successfully", Toast.LENGTH_SHORT).show()

          if (it.user?.isEmailVerified == false) {
            it.user.sendEmailVerification().addOnCompleteListener {
              if (it.isSuccessful) {
                Toast.makeText(context, "Verification email sent", Toast.LENGTH_SHORT).show()
              } else {
                Toast.makeText(context, "Failed to send verification email", Toast.LENGTH_SHORT)
                    .show()
                Log.e("WelcomeScreen", "Failed to send verification email", it.exception)
              }
            }
          }
        }
        SignInState.SUCCESS_CREATE_ACCOUNT -> {
          Toast.makeText(context, "Account created successfully", Toast.LENGTH_SHORT).show()

          it.user?.sendEmailVerification()?.addOnCompleteListener {
            if (it.isSuccessful) {
              Toast.makeText(context, "Verification email sent", Toast.LENGTH_SHORT).show()
            } else {
              Toast.makeText(context, "Failed to send verification email", Toast.LENGTH_SHORT)
                  .show()
              Log.e("WelcomeScreen", "Failed to send verification email", it.exception)
            }
          }
        }
      }
    }
  }

  val enabled = isValidEmail(email) && password.isNotEmpty()

  Scaffold(
      modifier = Modifier.testTag("WelcomeScreen").fillMaxSize(),
      content = { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
              Box(modifier = Modifier.clip(CircleShape).size(150.dp).background(Color.Gray))
              Spacer(modifier = Modifier.size(20.dp))

              Text(
                  "The world’s largest campus life platform.",
                  style = AppTypography.titleLarge,
                  modifier = Modifier.fillMaxWidth(0.8f),
                  textAlign = Center)

              Spacer(modifier = Modifier.size(50.dp))
              Text("Sign up or log in to get started.", style = AppTypography.titleSmall)

              OutlinedTextField(
                  modifier = Modifier.testTag("WelcomeEmail"),
                  value = email,
                  onValueChange = { email = it },
                  label = { Text("Enter your email address") },
                  placeholder = { Text("john.doe@epfl.ch") },
              )

              OutlinedTextField(
                  modifier = Modifier.testTag("WelcomePassword"),
                  value = password,
                  onValueChange = { password = it },
                  label = { Text("Enter your password") },
                trailingIcon = {
                  IconButton(
                    onClick = { showPassword = !showPassword },
                  ) {
                    Icon(
                      if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                      contentDescription = if (showPassword) "Hide password" else "Show password",
                    )
                  }
                },
                  visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
              )

              Spacer(modifier = Modifier.size(70.dp))

              Button(
                  modifier = Modifier.testTag("WelcomeButton"),
                  onClick = { signIn() },
                  enabled = enabled) {
                    Text("Continue")
                  }
            }
      })
}
