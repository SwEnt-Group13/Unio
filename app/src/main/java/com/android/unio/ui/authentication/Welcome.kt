package com.android.unio.ui.authentication

import android.content.Context
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.unit.dp
import com.android.unio.R
import com.android.unio.model.strings.test_tags.WelcomeTestTags
import com.android.unio.model.user.SignInState
import com.android.unio.model.user.UserViewModel
import com.android.unio.model.user.isValidEmail
import com.android.unio.model.user.isValidPassword
import com.android.unio.model.user.signInOrCreateAccount
import com.android.unio.model.utils.Utils.checkInternetConnection
import com.android.unio.ui.theme.AppTypography
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.auth

@Composable
fun WelcomeScreen(userViewModel: UserViewModel) {
  val context = LocalContext.current

  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }

  var showPassword by remember { mutableStateOf(false) }

  val validEmail = isValidEmail(email)
  val validPassword = isValidPassword(password)
  val enabled = validEmail && validPassword

  val passwordError = !validPassword && password.isNotEmpty()

  Scaffold(
      modifier = Modifier.testTag(WelcomeTestTags.SCREEN).fillMaxSize(),
      content = { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
              Box(modifier = Modifier.clip(CircleShape).size(150.dp).background(Color.Gray))
              Spacer(modifier = Modifier.size(20.dp))

              Text(
                  context.getString(R.string.welcome_message),
                  style = AppTypography.titleLarge,
                  modifier = Modifier.fillMaxWidth(0.8f),
                  textAlign = Center)

              Spacer(modifier = Modifier.size(50.dp))
              Text(
                  context.getString(R.string.welcome_login_signup),
                  style = AppTypography.titleMedium)

              OutlinedTextField(
                  modifier = Modifier.testTag(WelcomeTestTags.EMAIL),
                  value = email,
                  onValueChange = { email = it },
                  keyboardOptions =
                      KeyboardOptions(
                          keyboardType = KeyboardType.Email,
                          imeAction = ImeAction.Done,
                          capitalization = KeyboardCapitalization.None),
                  singleLine = true,
                  label = { Text(context.getString(R.string.welcome_email_enter)) },
                  placeholder = { Text(context.getString(R.string.welcome_email_display)) },
              )

              OutlinedTextField(
                  modifier = Modifier.testTag(WelcomeTestTags.PASSWORD),
                  value = password,
                  onValueChange = { password = it },
                  keyboardOptions =
                      KeyboardOptions(
                          keyboardType = KeyboardType.Password,
                          imeAction = ImeAction.Done,
                          capitalization = KeyboardCapitalization.None),
                  singleLine = true,
                  label = { Text(context.getString(R.string.welcome_password_enter)) },
                  isError = passwordError,
                  supportingText = {
                    if (passwordError) {
                      Text(context.getString(R.string.welcome_password_error_correction))
                    }
                  },
                  trailingIcon = {
                    IconButton(
                        onClick = { showPassword = !showPassword },
                    ) {
                      Icon(
                          if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                          contentDescription =
                              if (showPassword)
                                  context.getString(
                                      R.string.welcome_content_description_hide_password)
                              else
                                  context.getString(
                                      R.string.welcome_content_description_show_password),
                      )
                    }
                  },
                  visualTransformation =
                      if (showPassword) VisualTransformation.None
                      else PasswordVisualTransformation(),
              )

              Spacer(modifier = Modifier.size(70.dp))

              Button(
                  modifier = Modifier.testTag(WelcomeTestTags.BUTTON),
                  onClick = {
                    if (!enabled) {
                      Toast.makeText(
                              context,
                              context.getString(R.string.welcome_malformed_email_password),
                              Toast.LENGTH_SHORT)
                          .show()
                    } else {
                      userViewModel.setCredential(EmailAuthProvider.getCredential(email, password))
                      handleAuthentication(email, password, context)
                    }
                  },
                  enabled = enabled) {
                    Text(context.getString(R.string.welcome_button_continue))
                  }
            }
      })
}

fun handleAuthentication(email: String, password: String, context: Context) {

  // Check internet connectivity
  val isConnected = checkInternetConnection(context)
  if (!isConnected) {
    Toast.makeText(context, "You appear to be offline.", Toast.LENGTH_SHORT).show()
    return
  }

  signInOrCreateAccount(email, password, Firebase.auth) { signInResult ->
    // NOTE: No need to navigate to other screens, that is already handled by the listener in
    // MainActivity
    when (signInResult.state) {
      SignInState.INVALID_CREDENTIALS -> {
        Toast.makeText(
                context,
                context.getString(R.string.welcome_toast_invalid_creds),
                Toast.LENGTH_SHORT)
            .show()
      }
      SignInState.INVALID_EMAIL_FORMAT -> {
        Toast.makeText(
                context,
                context.getString(R.string.welcome_toast_enter_valid_email),
                Toast.LENGTH_SHORT)
            .show()
      }
      SignInState.SUCCESS_SIGN_IN -> {
        Toast.makeText(
                context, context.getString(R.string.welcome_toast_sign_in), Toast.LENGTH_SHORT)
            .show()

        if (signInResult.user?.isEmailVerified == false) {
          signInResult.user.sendEmailVerification().addOnCompleteListener {
            if (it.isSuccessful) {
              Toast.makeText(
                      context,
                      context.getString(R.string.welcome_toast_verification_sent),
                      Toast.LENGTH_SHORT)
                  .show()
            } else {
              Toast.makeText(
                      context,
                      context.getString(R.string.welcome_toast_verification_sent_failed),
                      Toast.LENGTH_SHORT)
                  .show()
              Log.e("WelcomeScreen", "Failed to send verification email", it.exception)
            }
          }
        }
      }
      SignInState.SUCCESS_CREATE_ACCOUNT -> {
        Toast.makeText(
                context,
                context.getString(R.string.welcome_toast_account_created),
                Toast.LENGTH_SHORT)
            .show()

        signInResult.user?.sendEmailVerification()?.addOnCompleteListener {
          if (it.isSuccessful) {
            Toast.makeText(
                    context,
                    context.getString(R.string.welcome_toast_verification_sent),
                    Toast.LENGTH_SHORT)
                .show()
          } else {
            Toast.makeText(
                    context,
                    context.getString(R.string.welcome_toast_verification_sent_failed),
                    Toast.LENGTH_SHORT)
                .show()
            Log.e("WelcomeScreen", "Failed to send verification email", it.exception)
          }
        }
      }
    }
  }
}
