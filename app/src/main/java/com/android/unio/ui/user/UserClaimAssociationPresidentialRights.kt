package com.android.unio.ui.user

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.unio.R
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.strings.test_tags.user.UserClaimAssociationPresidentialRightsTestTags
import com.android.unio.model.user.User
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.association.AssociationSearchBar
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.theme.AppTypography
import com.android.unio.ui.theme.errorLight
import com.android.unio.ui.user.UserClaimAssociationPresidentialRightsParserStrings.ASSOCIATION_UID
import com.android.unio.ui.user.UserClaimAssociationPresidentialRightsParserStrings.CODE
import com.android.unio.ui.user.UserClaimAssociationPresidentialRightsParserStrings.EMAIL
import com.android.unio.ui.user.UserClaimAssociationPresidentialRightsParserStrings.SEND_VERIFICATION_EMAIL
import com.android.unio.ui.user.UserClaimAssociationPresidentialRightsParserStrings.USER_UID
import com.android.unio.ui.user.UserClaimAssociationPresidentialRightsParserStrings.VERIFY_CODE
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.functions
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserClaimAssociationPresidentialRightsScreen(
    navigationAction: NavigationAction,
    associationViewModel: AssociationViewModel,
    userViewModel: UserViewModel,
    searchViewModel: SearchViewModel
) {
  val user by userViewModel.user.collectAsState()
  if (user == null) {
    return
  }
  UserClaimAssociationPresidentialRightsScreenScaffold(
      navigationAction, associationViewModel, user!!, searchViewModel)
}

/**
 * Composable function that displays the main UI scaffold for the "Claim Association Presidential
 * Rights" screen.
 *
 * This function provides the structure and logic for the user to claim presidential rights for an
 * association. It includes steps for selecting an association, verifying the user's email address,
 * and entering a verification code. Upon successful verification, the user is granted the
 * appropriate administrative rights.
 *
 * @param navigationAction Provides navigation actions to navigate between screens.
 * @param associationViewModel ViewModel for managing the association's state and interactions.
 * @param user The current user interacting with the screen.
 * @param searchViewModel ViewModel for managing the search functionality for associations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserClaimAssociationPresidentialRightsScreenScaffold(
    navigationAction: NavigationAction,
    associationViewModel: AssociationViewModel,
    user: User,
    searchViewModel: SearchViewModel
) {
  val context = LocalContext.current
  val association by associationViewModel.selectedAssociation.collectAsState()

  // State variables to hold the user input and verification status
  var email by remember { mutableStateOf("") }
  var isEmailVerified by remember { mutableStateOf(false) }
  var isAssociationChosen by remember { mutableStateOf(false) }
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
                  modifier =
                      Modifier.testTag(
                          UserClaimAssociationPresidentialRightsTestTags.ASSOCIATION_PROFILE_TITLE))
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationAction.goBack() },
                  modifier =
                      Modifier.testTag(
                          UserClaimAssociationPresidentialRightsTestTags.GO_BACK_BUTTON)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = context.getString(R.string.association_go_back))
                  }
            })
      },
      content = { padding ->
        Surface(
            modifier = Modifier.fillMaxWidth().padding(padding),
        ) {
          Column(
              modifier = Modifier.padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text(
                context.getString(
                    R.string.user_claim_association_presidential_rights_claim_presidential_rights),
                style = AppTypography.headlineSmall)

            Spacer(modifier = Modifier.height(16.dp))

            if (!isAssociationChosen) {
              val focusRequester = remember { FocusRequester() }

              LaunchedEffect(Unit) { focusRequester.requestFocus() }

              Box(modifier = Modifier.focusRequester(focusRequester)) {
                AssociationSearchBar(
                    searchViewModel = searchViewModel,
                    onAssociationSelected = { association ->
                      associationViewModel.selectAssociation(association.uid)
                      isAssociationChosen = true
                    },
                    false,
                    {})
              }
            } else {
              if (association == null) {
                isAssociationChosen = false
              }
              // Step 1 ->>> Ask for the presidential email address if it hasn't been verified
              if (!isEmailVerified) {
                Text(
                    context.getString(
                        R.string
                            .user_claim_association_presidential_rights_enter_presidential_email),
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
                      color = errorLight,
                      style = AppTypography.bodySmall)
                }
                val coroutineScope = rememberCoroutineScope()

                Button(
                    onClick = {
                      if (email == association!!.principalEmailAddress) {
                        isEmailVerified = true
                        showErrorMessage = false

                        // send verification email
                        coroutineScope.launch {
                          sendVerificationEmail(Firebase.functions, email, association!!.uid)
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
                                }
                              }
                        }
                      } else {
                        // email does not match principalEmailAddress
                        showErrorMessage = true
                      }
                    },
                    modifier =
                        Modifier.padding(vertical = 8.dp)
                            .testTag(
                                UserClaimAssociationPresidentialRightsTestTags
                                    .VERIFY_EMAIL_BUTTON)) {
                      Text(
                          context.getString(
                              R.string.user_claim_association_presidential_rights_verify_email))
                    }
              } else {
                // Step 2 ->>> If email is verified, ask for the verification code
                Text(
                    context.getString(
                        R.string.user_claim_association_presidential_rights_enter_code_sent_to) +
                        " $email:",
                    style = AppTypography.bodySmall)
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
                      coroutineScope.launch {
                        verifyCode(
                                Firebase.functions, association!!.uid, verificationCode, user!!.uid)
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
                    },
                    modifier =
                        Modifier.padding(vertical = 8.dp)
                            .testTag(
                                UserClaimAssociationPresidentialRightsTestTags
                                    .SUBMIT_CODE_BUTTON)) {
                      Text(
                          context.getString(
                              R.string.user_claim_association_presidential_rights_submit_code))
                    }
              }
            }
          }
        }
      })
}

/** String manager for the verifyCode and sendVerificationEmail functions */
private object UserClaimAssociationPresidentialRightsParserStrings {
  // Verification code firebase function
  const val VERIFY_CODE = "verifyCode"
  const val CODE = "code"
  const val USER_UID = "userUid"

  // Send verification email firebase function
  const val SEND_VERIFICATION_EMAIL = "sendVerificationEmail"
  const val EMAIL = "email"

  // Common strings
  const val ASSOCIATION_UID = "associationUid"
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
      .getHttpsCallable(VERIFY_CODE)
      .call(hashMapOf(ASSOCIATION_UID to associationUid, CODE to code, USER_UID to userUid))
      .continueWith { task ->
        val result = task.result?.data as String
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
      .getHttpsCallable(SEND_VERIFICATION_EMAIL)
      .call(hashMapOf(EMAIL to userEmail, ASSOCIATION_UID to associationUid))
      .continueWith { task ->
        val result = task.result?.data as String
        result
      }
}
