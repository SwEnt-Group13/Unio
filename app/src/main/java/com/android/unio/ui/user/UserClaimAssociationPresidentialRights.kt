package com.android.unio.ui.user

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.unio.R
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.strings.test_tags.UserClaimAssociationPresidentialRightsTestTags
import com.android.unio.model.user.User
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.theme.AppTypography
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.functions
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserClaimAssociationPresidentialRightsScreen(
    associationViewModel: AssociationViewModel,
    navigationAction: NavigationAction,
    userViewModel: UserViewModel
) {
  val association by associationViewModel.selectedAssociation.collectAsState()
  val user by userViewModel.user.collectAsState()

  association?.let {
    user?.let { it1 ->
      UserClaimAssociationPresidentialRightsScreenScaffold(navigationAction, it, it1)
    } ?: Log.e("YourTag", "User is null")
  } ?: Log.e("YourTag", "Association is null")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserClaimAssociationPresidentialRightsScreenScaffold(
    navigationAction: NavigationAction,
    association: Association,
    user: User
) {
  val context = LocalContext.current

  // State variables to hold the user input and verification status
  var email by remember { mutableStateOf("") }
  var isEmailVerified by remember { mutableStateOf(false) }
  var verificationCode by remember { mutableStateOf("") }
  var showErrorMessage by remember { mutableStateOf(false) }

  Scaffold(
      modifier = Modifier.testTag(UserClaimAssociationPresidentialRightsTestTags.SCREEN),
      topBar = {
        TopAppBar(
            title = {
              Text(
                  text =
                      context.getString(
                          R.string.user_claim_association_presidential_rights_go_back),
                  modifier = Modifier.testTag("AssociationProfileTitle"))
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationAction.goBack() },
                  modifier = Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = context.getString(R.string.association_go_back))
                  }
            },
            actions = {
              Row {
                IconButton(onClick = {}) {
                  Icon(
                      Icons.Outlined.MoreVert,
                      contentDescription = context.getString(R.string.association_see_more))
                }
              }
            })
      },
      content = { padding ->
        Surface(
            modifier = Modifier.padding(padding),
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
                context.getString(
                    R.string.user_claim_association_presidential_rights_claim_presidential_rights),
                style = AppTypography.headlineSmall)

            Spacer(modifier = Modifier.height(16.dp))

            // Step 1 ->>> Ask for the presidential email address if it hasn't been verified
            if (!isEmailVerified) {
              Text(
                  context.getString(
                      R.string.user_claim_association_presidential_rights_enter_presidential_email),
                  style = AppTypography.bodySmall)
              TextField(
                  value = email,
                  onValueChange = { email = it },
                  placeholder = {
                    Text(
                        context.getString(
                            R.string.user_claim_association_presidential_rights_email_address))
                  },
                  isError = showErrorMessage,
                  modifier =
                      Modifier.padding(vertical = 8.dp)
                          .testTag(UserClaimAssociationPresidentialRightsTestTags.EMAIL_ADDRESS))
              Spacer(modifier = Modifier.height(8.dp))

              // if the email is incorrect
              if (showErrorMessage) {
                Text(
                    text =
                        context.getString(
                            R.string
                                .user_claim_association_presidential_rights_incorrect_email_error),
                    color = androidx.compose.ui.graphics.Color.Red,
                    style = AppTypography.bodySmall)
              }
              val coroutineScope = rememberCoroutineScope()

              Button(
                  onClick = {
                    if (association != null) {
                      if (user != null) {
                        if (email == association!!.principalEmailAddress) {
                          isEmailVerified = true
                          showErrorMessage = false

                          // send verification email
                          coroutineScope.launch {
                            sendVerificationEmail(
                                    Firebase.functions, user!!.email, association!!.uid)
                                .addOnCompleteListener { task ->
                                  if (!task.isSuccessful) {
                                    val e = task.exception
                                    if (e is FirebaseFunctionsException) {
                                      val code = e.code
                                      val details = e.details
                                      Log.e(
                                          "CloudFunctionError",
                                          "Error Code: $code, Details: $details",
                                          e)
                                    } else {
                                      Log.e(
                                          "CloudFunctionError",
                                          context.getString(
                                              R.string
                                                  .user_claim_association_presidential_rights_unexpected_error),
                                          e)
                                    }
                                  } else {
                                    // it works !
                                  }
                                }
                          }
                        } else {
                          // email does not match principalEmailAddress
                          showErrorMessage = true
                        }
                      } else {
                        showErrorMessage = true
                        Log.e("UserError", "User does not exist or has no email")
                      }
                    } else {
                      // association is null
                      showErrorMessage = true
                      Log.e(
                          "AssociationError",
                          "Association does not exist or has no principalEmailAddress")
                    }
                  },
                  modifier =
                      Modifier.padding(vertical = 8.dp)
                          .testTag(
                              UserClaimAssociationPresidentialRightsTestTags.VERIFY_EMAIL_BUTTON)) {
                    Text("Verify Email")
                  }
            } else {
              // Step 2 ->>> If email is verified, ask for the verification code
              Text("Enter the code sent to $email:", style = AppTypography.bodySmall)
              TextField(
                  value = verificationCode,
                  onValueChange = { verificationCode = it },
                  placeholder = {
                    Text(
                        context.getString(
                            R.string
                                .user_claim_association_presidential_rights_enter_verification_code))
                  },
                  modifier =
                      Modifier.padding(vertical = 8.dp)
                          .testTag(UserClaimAssociationPresidentialRightsTestTags.CODE))
              Spacer(modifier = Modifier.height(8.dp))

              val coroutineScope = rememberCoroutineScope()

              Button(
                  onClick = {
                    if (association != null) {
                      if (user != null) {
                        coroutineScope.launch {
                          verifyCode(
                                  Firebase.functions,
                                  association!!.uid,
                                  verificationCode,
                                  user!!.uid)
                              .addOnCompleteListener { task ->
                                if (!task.isSuccessful) {
                                  val e = task.exception
                                  if (e is FirebaseFunctionsException) {
                                    val code = e.code
                                    Log.e("CloudFunctionError", "Error Code: $code", e)

                                    when (code) {
                                      FirebaseFunctionsException.Code.INVALID_ARGUMENT -> {
                                        Toast.makeText(
                                                context,
                                                context.getString(
                                                    R.string
                                                        .user_claim_association_presidential_rights_wrong_code_error),
                                                Toast.LENGTH_SHORT)
                                            .show()
                                      }
                                      FirebaseFunctionsException.Code.NOT_FOUND -> {
                                        Toast.makeText(
                                                context,
                                                context.getString(
                                                    R.string
                                                        .user_claim_association_presidential_rights_verification_request_not_found),
                                                Toast.LENGTH_SHORT)
                                            .show()
                                      }
                                      FirebaseFunctionsException.Code.UNAVAILABLE -> {
                                        Toast.makeText(
                                                context,
                                                context.getString(
                                                    R.string
                                                        .user_claim_association_presidential_rights_service_unavailable),
                                                Toast.LENGTH_SHORT)
                                            .show()
                                      }
                                      else -> {
                                        Toast.makeText(
                                                context,
                                                context.getString(
                                                    R.string
                                                        .user_claim_association_presidential_rights_unexpected_error),
                                                Toast.LENGTH_SHORT)
                                            .show()
                                      }
                                    }
                                  } else {
                                    Toast.makeText(
                                            context,
                                            context.getString(
                                                R.string
                                                    .user_claim_association_presidential_rights_unexpected_error),
                                            Toast.LENGTH_SHORT)
                                        .show()
                                  }
                                } else {
                                  Log.d("CloudFunction", "OK")
                                  Toast.makeText(
                                          context,
                                          context.getString(
                                              R.string
                                                  .user_claim_association_presidential_rights_verified_successfully),
                                          Toast.LENGTH_SHORT)
                                      .show()
                                  navigationAction.navigateTo(Screen.MY_PROFILE)
                                }
                              }
                        }
                      } else {
                        Log.e("UserError", "User does not exist or has no uid")
                        Toast.makeText(
                                context,
                                context.getString(
                                    R.string
                                        .user_claim_association_presidential_rights_unexpected_error),
                                Toast.LENGTH_SHORT)
                            .show()
                      }
                    } else {
                      Log.e(
                          "AssociationError",
                          "Association does not exist or has no principalEmailAddress")
                      Toast.makeText(
                              context,
                              context.getString(
                                  R.string
                                      .user_claim_association_presidential_rights_unexpected_error),
                              Toast.LENGTH_SHORT)
                          .show()
                    }
                  },
                  modifier =
                      Modifier.padding(vertical = 8.dp)
                          .testTag(
                              UserClaimAssociationPresidentialRightsTestTags.SUBMIT_CODE_BUTTON)) {
                    Text(
                        context.getString(
                            R.string.user_claim_association_presidential_rights_submit_code))
                  }
            }
          }
        }
      })
}

/**
 * This function verify if the code given is the right one & update admin rights for the user
 * accordingly It also respects the timing of 10 minutes of validity of a given code
 */
private fun verifyCode(
    functions: FirebaseFunctions,
    associationUid: String,
    code: String,
    userUid: String
): Task<String> {
  // the continuation runs on either success or failure :)
  return functions
      .getHttpsCallable("verifyCode")
      .call(hashMapOf("associationUid" to associationUid, "code" to code, "userUid" to userUid))
      .continueWith { task ->
        val result = task.result?.getData() as String
        result
      }
}

/**
 * This function send the verification email with my email for the moment (all the sensitive
 * information is hold in github secrets) with a random 6-digit code
 */
private fun sendVerificationEmail(
    functions: FirebaseFunctions,
    userEmail: String,
    associationUid: String
): Task<String> {
  // the continuation runs on either success or failure :)
  return functions
      .getHttpsCallable("sendVerificationEmail")
      .call(hashMapOf("email" to userEmail, "associationUid" to associationUid))
      .continueWith { task ->
        val result = task.result?.getData() as String
        result
      }
}
