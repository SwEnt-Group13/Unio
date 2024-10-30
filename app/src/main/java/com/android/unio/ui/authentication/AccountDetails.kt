package com.android.unio.ui.accountCreation

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.unio.model.association.Association
import com.android.unio.model.firestore.emptyFirestoreReferenceList
import com.android.unio.model.user.AccountDetailsError
import com.android.unio.model.user.Interest
import com.android.unio.model.user.User
import com.android.unio.model.user.UserSocial
import com.android.unio.model.user.UserViewModel
import com.android.unio.model.user.checkNewUser
import com.android.unio.ui.authentication.overlay.InterestOverlay
import com.android.unio.ui.authentication.overlay.SocialOverlay
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.theme.AppTypography
import com.android.unio.ui.theme.primaryLight
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountDetails(
    navigationAction: NavigationAction,
    userViewModel: UserViewModel
) {
  var firstName: String by remember { mutableStateOf("") }
  var lastName: String by remember { mutableStateOf("") }
  var bio: String by remember { mutableStateOf("") }

  var isErrors by remember { mutableStateOf(mutableSetOf<AccountDetailsError>()) }

  val interestsFlow = remember {
    MutableStateFlow(Interest.entries.map { it to mutableStateOf(false) }.toList())
  }

  val userSocialsFlow = remember { MutableStateFlow(emptyList<UserSocial>().toMutableList()) }

  val interests by interestsFlow.collectAsState()
  val socials by userSocialsFlow.collectAsState()

  val context = LocalContext.current
  var showInterestsOverlay by remember { mutableStateOf(false) }
  var showSocialsOverlay by remember { mutableStateOf(false) }
  val scrollState = rememberScrollState()

  if (Firebase.auth.currentUser == null) {
    navigationAction.navigateTo(Screen.WELCOME)
    return
  }
  Column(
      modifier =
          Modifier.padding(vertical = 20.dp, horizontal = 40.dp)
              .verticalScroll(scrollState)
              .testTag("AccountDetails"),
      verticalArrangement = Arrangement.SpaceBetween,
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Tell us about yourself",
            style = AppTypography.headlineSmall,
            modifier = Modifier.testTag("AccountDetailsTitleText"))

        val isFirstNameError = isErrors.contains(AccountDetailsError.EMPTY_FIRST_NAME)
        OutlinedTextField(
            modifier =
                Modifier.padding(4.dp).fillMaxWidth().testTag("AccountDetailsFirstNameTextField"),
            label = {
              Text("First name", modifier = Modifier.testTag("AccountDetailsFirstNameText"))
            },
            isError = (isFirstNameError),
            supportingText = {
              if (isFirstNameError) {
                Text(
                    AccountDetailsError.EMPTY_FIRST_NAME.errorMessage,
                    modifier = Modifier.testTag("AccountDetailsFirstNameErrorText"))
              }
            },
            onValueChange = { firstName = it },
            value = firstName)
        val isLastNameError = isErrors.contains(AccountDetailsError.EMPTY_LAST_NAME)
        OutlinedTextField(
            modifier =
                Modifier.padding(4.dp).fillMaxWidth().testTag("AccountDetailsLastNameTextField"),
            label = {
              Text("Last name", modifier = Modifier.testTag("AccountDetailsLastNameText"))
            },
            isError = (isLastNameError),
            supportingText = {
              if (isLastNameError) {
                Text(
                    AccountDetailsError.EMPTY_LAST_NAME.errorMessage,
                    modifier = Modifier.testTag("AccountDetailsLastNameErrorText"))
              }
            },
            onValueChange = { lastName = it },
            value = lastName)
        OutlinedTextField(
            modifier =
                Modifier.padding(4.dp)
                    .fillMaxWidth()
                    .height(200.dp)
                    .testTag("AccountDetailsBioTextField"),
            label = { Text("Bio", modifier = Modifier.testTag("AccountDetailsBioText")) },
            onValueChange = { bio = it },
            value = bio)

        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically) {
              Text(
                  text = "Maybe add a profile picture?",
                  modifier =
                      Modifier.widthIn(max = 140.dp).testTag("AccountDetailsProfilePictureText"),
                  style = AppTypography.bodyLarge)
              Icon(
                  Icons.Rounded.AccountCircle,
                  contentDescription = "Add",
                  tint = primaryLight,
                  modifier =
                      Modifier.clickable {
                            Toast.makeText(context, "Not yet implemented", Toast.LENGTH_SHORT)
                                .show()
                          }
                          .size(100.dp)
                          .testTag("AccountDetailsProfilePictureIcon"))
            }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth().testTag("AccountDetailsInterestsButton"),
            onClick = { showInterestsOverlay = true }) {
              Icon(Icons.Default.Add, contentDescription = "Add")
              Text("Add centers of interest")
            }
        FlowRow {
          interests.forEachIndexed { index, pair ->
            if (pair.second.value) {
              InputChip(
                  label = { Text(pair.first.name) },
                  onClick = {},
                  selected = pair.second.value,
                  modifier = Modifier.padding(3.dp).testTag("AccountDetailsInterestChip: $index"),
                  avatar = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Add",
                        modifier = Modifier.clickable { pair.second.value = !pair.second.value })
                  })
            }
          }
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth().testTag("AccountDetailsSocialsButton"),
            onClick = { showSocialsOverlay = true }) {
              Icon(Icons.Default.Add, contentDescription = "Add")
              Text("Add links to other social media")
            }
        FlowRow(modifier = Modifier.fillMaxWidth()) {
          socials.forEachIndexed { index, userSocial ->
            InputChip(
                label = { Text(userSocial.social.name) },
                onClick = {},
                selected = true,
                modifier = Modifier.padding(3.dp).testTag("AccountDetailsSocialChip: $index"),
                avatar = {
                  Icon(
                      Icons.Default.Close,
                      contentDescription = "Add",
                      modifier =
                          Modifier.clickable {
                            userSocialsFlow.value =
                                userSocialsFlow.value.toMutableList().apply { removeAt(index) }
                          })
                })
          }
        }
        Button(
            modifier = Modifier.testTag("AccountDetailsContinueButton"),
            onClick = {
              val user =
                  User(
                      uid = Firebase.auth.currentUser?.uid!!,
                      email = Firebase.auth.currentUser?.email!!,
                      firstName = firstName,
                      lastName = lastName,
                      biography = bio,
                      followedAssociations = Association.emptyFirestoreReferenceList(),
                      joinedAssociations = Association.emptyFirestoreReferenceList(),
                      interests = interests.filter { it.second.value }.map { it.first },
                      socials = socials,
                      profilePicture = "")
              isErrors = checkNewUser(user)
              if (isErrors.isEmpty()) {
                userViewModel.addUser(
                    user,
                    onSuccess = {
                      Toast.makeText(context, "Account Created Successfully", Toast.LENGTH_SHORT)
                          .show()
                      navigationAction.navigateTo(Screen.HOME)
                    })
              }
            }) {
              Text("Continue")
            }
      }

  if (showInterestsOverlay) {
    InterestOverlay(
        onDismiss = { showInterestsOverlay = false },
        onSave = { newInterests ->
          interestsFlow.value = newInterests
          showInterestsOverlay = false
        },
        interests = interests)
  }

  if (showSocialsOverlay) {
    SocialOverlay(
        onDismiss = { showSocialsOverlay = false },
        onSave = { newUserSocials ->
          userSocialsFlow.value = newUserSocials
          showSocialsOverlay = false
        },
        userSocials = socials)
  }
}
