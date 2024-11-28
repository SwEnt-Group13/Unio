package com.android.unio.ui.authentication

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import com.android.unio.model.strings.test_tags.ResetPasswordTestTags
import com.android.unio.model.user.isValidEmail
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.theme.AppTypography

@Composable
fun ResetPasswordScreen(navigationAction: NavigationAction, authViewModel: AuthViewModel) {

  val context = LocalContext.current

  ResetPasswordContent(
      onChangePassword = { email ->
        authViewModel.sendEmailResetPassword(
            email,
            onSuccess = {
              Toast.makeText(
                      context,
                      context.getString(R.string.reset_password_success_toast),
                      Toast.LENGTH_SHORT)
                  .show()
            },
            onFailure = {
              Toast.makeText(
                      context,
                      context.getString(R.string.reset_password_error_toast),
                      Toast.LENGTH_SHORT)
                  .show()
            })
        navigationAction.goBack()
      },
      onDismiss = { navigationAction.goBack() })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordContent(
    onDismiss: () -> Unit,
    onChangePassword: (String) -> Unit,
) {
  val context = LocalContext.current

  Scaffold(
      modifier = Modifier.testTag(ResetPasswordTestTags.SCREEN),
      topBar = {
        TopAppBar(
            title = {},
            navigationIcon = {
              IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = context.getString(R.string.reset_password_go_back))
              }
            })
      }) { padding ->
        var email: String by remember { mutableStateOf("") }
        var invalidEmail: Boolean by remember { mutableStateOf(false) }

        Column(
            modifier =
                Modifier.padding(padding)
                    .fillMaxSize()
                    .padding(vertical = 20.dp, horizontal = 40.dp),
            verticalArrangement = Arrangement.spacedBy(30.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                  text = context.getString(R.string.reset_password_text),
                  textAlign = TextAlign.Center,
                  style = AppTypography.headlineSmall)

              OutlinedTextField(
                  modifier =
                      Modifier.padding(4.dp)
                          .fillMaxWidth()
                          .testTag(ResetPasswordTestTags.EMAIL_FIELD),
                  label = {
                    Text(
                        text = context.getString(R.string.reset_password_email_place_holder),
                        style = AppTypography.bodySmall,
                        modifier = Modifier.testTag(ResetPasswordTestTags.EMAIL_TEXT))
                  },
                  isError = (invalidEmail),
                  supportingText = {
                    if (invalidEmail) {
                      Text(
                          context.getString(R.string.reset_password_invalid_email),
                          modifier = Modifier.testTag(ResetPasswordTestTags.EMAIL_ERROR_TEXT))
                    }
                  },
                  onValueChange = { email = it },
                  value = email)

              Button(
                  modifier = Modifier.testTag(ResetPasswordTestTags.RESET_PASSWORD_BUTTON),
                  onClick = {
                    if (isValidEmail(email)) {
                      invalidEmail = false
                      onChangePassword(email)
                    } else {
                      invalidEmail = true
                    }
                  }) {
                    Text(context.getString(R.string.reset_password_button))
                  }
            }
      }
}
