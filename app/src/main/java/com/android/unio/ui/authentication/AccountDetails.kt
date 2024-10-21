package com.android.unio.ui.accountCreation

import android.content.Context
import android.util.Log
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.android.unio.model.association.Association
import com.android.unio.model.firestore.emptyFirestoreReferenceList
import com.android.unio.model.user.Interest
import com.android.unio.model.user.User
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserSocial
import com.android.unio.ui.authentication.InterestOverlay
import com.android.unio.ui.authentication.SocialsOverlay
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.theme.AppTypography
import com.android.unio.ui.theme.primaryLight
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow


@Preview(showBackground = true)
@Composable
fun AccountDetailsPreview() {
    val navController = rememberNavController()
    val navigationActions = NavigationAction(navController)
    val userRepositoryFirestore = UserRepositoryFirestore(Firebase.firestore)
    AccountDetails(
        navigationAction = navigationActions,
        userRepositoryFirestore = userRepositoryFirestore
    )
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountDetails(
    navigationAction: NavigationAction,
    userRepositoryFirestore: UserRepositoryFirestore,
) {
  var firstName: String by remember { mutableStateOf("") }
  var lastName: String by remember { mutableStateOf("") }
  var bio: String by remember { mutableStateOf("") }

  val interestsFlow = remember {
      MutableStateFlow(Interest.entries.map { it to mutableStateOf(false) }.toMutableList())
  }

    val userSocialsFlow: MutableStateFlow<MutableList<UserSocial>> = remember {
        MutableStateFlow(emptyList<UserSocial>().toMutableList())
    }

  val interests by interestsFlow.collectAsState()
    val socials by userSocialsFlow.collectAsState()

  val context = LocalContext.current
  var showInterestsOverlay by remember { mutableStateOf(false) }
    var showSocialsOverlay by remember{ mutableStateOf(false) }
  val scrollState = rememberScrollState()

  if (Firebase.auth.currentUser == null) {
    navigationAction.navigateTo(Screen.WELCOME)
    return
  }
  Column(
      modifier =
      Modifier
          .padding(vertical = 20.dp, horizontal = 40.dp)
          .verticalScroll(scrollState)
          .testTag("AccountDetails"),
      verticalArrangement = Arrangement.SpaceBetween,
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Tell us about yourself",
            style = AppTypography.headlineSmall,
            modifier = Modifier.testTag("AccountDetailsTitleText"))

        OutlinedTextField(
            modifier =
            Modifier
                .padding(4.dp)
                .fillMaxWidth()
                .testTag("AccountDetailsFirstNameTextField"),
            label = {
              Text("First name", modifier = Modifier.testTag("AccountDetailsFirstNameText"))
            },
            onValueChange = { firstName = it },
            value = firstName)
        OutlinedTextField(
            modifier =
            Modifier
                .padding(4.dp)
                .fillMaxWidth()
                .testTag("AccountDetailsLastNameTextField"),
            label = {
              Text("Last name", modifier = Modifier.testTag("AccountDetailsLastNameText"))
            },
            onValueChange = { lastName = it },
            value = lastName)
        OutlinedTextField(
            modifier =
            Modifier
                .padding(4.dp)
                .fillMaxWidth()
                .height(200.dp)
                .testTag("AccountDetailsBioTextField"),
            label = { Text("Bio", modifier = Modifier.testTag("AccountDetailsBioText")) },
            onValueChange = { bio = it },
            value = bio)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically) {
              Text(
                  text = "Maybe add a profile picture?",
                  modifier =
                  Modifier
                      .widthIn(max = 140.dp)
                      .testTag("AccountDetailsProfilePictureText"),
                  style = AppTypography.bodyLarge)
              Icon(
                  Icons.Rounded.AccountCircle,
                  contentDescription = "Add",
                  tint = primaryLight,
                  modifier =
                  Modifier
                      .clickable {
                          Toast
                              .makeText(context, "Not yet implemented", Toast.LENGTH_SHORT)
                              .show()
                      }
                      .size(100.dp)
                      .testTag("AccountDetailsProfilePictureIcon"))
            }
        OutlinedButton(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("AccountDetailsInterestsButton"),
            onClick = { showInterestsOverlay = true }) {
              Icon(Icons.Default.Add, contentDescription = "Add")
              Text("Add centers of interest")
            }
        FlowRow() {
          interests.forEachIndexed() { index, pair ->
            if (pair.second.value) {
              InputChip(
                  label = { Text(pair.first.name) },
                  onClick = {},
                  selected = pair.second.value,
                  modifier = Modifier
                      .padding(3.dp)
                      .testTag("AccountDetailsInterestChip: $index"),
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
            modifier = Modifier
                .fillMaxWidth()
                .testTag("AccountDetailsSocialsButton"),
            onClick = {showSocialsOverlay = true}) {
              Icon(Icons.Default.Add, contentDescription = "Add")
              Text("Add links to other social media")
            }
        FlowRow() {
          /* TODO row containing dynamic list of social media links */
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
                      socials = emptyList(),
                      profilePicture = "",
                      hasProvidedAccountDetails = true)
              uploadUser(user, userRepositoryFirestore, navigationAction, context)
              navigationAction.navigateTo(Screen.HOME)
            }) {
              Text("Continue")
            }
      }

  if (showInterestsOverlay) {
    InterestOverlay(
        onDismiss = { showInterestsOverlay = false },
        onSave = { showInterestsOverlay = false },
        interests = interestsFlow)
  }

    if(showSocialsOverlay){
        SocialsOverlay(
            onDismiss = {showSocialsOverlay = false},
            onSave = {showSocialsOverlay = false},
            userSocials = userSocialsFlow)
    }
}

fun uploadUser(
    user: User,
    userRepositoryFirestore: UserRepositoryFirestore,
    navigationAction: NavigationAction,
    context: Context
) {
  userRepositoryFirestore.updateUser(
      user,
      onSuccess = {
        Toast.makeText(context, "Account Created Successfully", Toast.LENGTH_SHORT).show()
        navigationAction.navigateTo(Screen.HOME)
      },
      onFailure = {
        Toast.makeText(context, "Failed to create Account", Toast.LENGTH_SHORT).show()
        Log.e("AccountDetails", "Failed to upload user", it)
      })
}
