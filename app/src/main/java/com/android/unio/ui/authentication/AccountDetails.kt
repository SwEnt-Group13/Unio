package com.android.unio.ui.accountCreation

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.android.unio.model.association.Association
import com.android.unio.model.firestore.emptyFirestoreReferenceList
import com.android.unio.model.user.Interest
import com.android.unio.model.user.User
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserSocial
import com.android.unio.ui.authentication.InterestOverlay
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.theme.AppTheme
import com.android.unio.ui.theme.AppTypography
import com.android.unio.ui.theme.primaryLight
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.forEach

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountDetails(
    navigationAction: NavigationAction,
    userRepositoryFirestore: UserRepositoryFirestore
) {
  var firstName: String by remember { mutableStateOf("") }
  var lastName: String by remember { mutableStateOf("") }
  var bio: String by remember { mutableStateOf("") }

  val interestsFlow = remember {
    MutableStateFlow(Interest.entries.map { it to mutableStateOf(false) }.toMutableList())
  }

  val interests by interestsFlow.collectAsState()

  //    if(Firebase.auth.currentUser == null){
  //        navigationAction.navigateTo(Screen.WELCOME)
  //        return
  //    }
  //    val uid = Firebase.auth.currentUser?.uid
  //    val email = Firebase.auth.currentUser?.email

  val context = LocalContext.current
  var showOverlay1 by remember { mutableStateOf(false) }

  val scrollState = rememberScrollState()
  Column(
      modifier = Modifier.padding(vertical = 20.dp, horizontal = 40.dp).verticalScroll(scrollState),
      verticalArrangement = Arrangement.SpaceBetween,
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Tell us about yourself", style = AppTypography.headlineSmall)

        OutlinedTextField(
            modifier = Modifier.padding(4.dp).fillMaxWidth(),
            label = { Text("First name") },
            onValueChange = { firstName = it },
            value = firstName)
        OutlinedTextField(
            modifier = Modifier.padding(4.dp).fillMaxWidth(),
            label = { Text("Last name") },
            onValueChange = { lastName = it },
            value = lastName)
        OutlinedTextField(
            modifier = Modifier.padding(4.dp).fillMaxWidth().height(200.dp),
            label = { Text("Bio") },
            onValueChange = { bio = it },
            value = bio)

        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically) {
              Text(
                  text = "Maybe add a profile picture?",
                  modifier = Modifier.widthIn(max = 140.dp),
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
                          .size(100.dp))
            }
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { showOverlay1 = true }) {
          Icon(Icons.Default.Add, contentDescription = "Add")
          Text("Add centers of interest")
        }
        FlowRow() {
          interests.forEach { pair ->
            if (pair.second.value) {
              InputChip(
                  label = { Text(pair.first.name) },
                  onClick = {},
                  selected = pair.second.value,
                  modifier = Modifier.padding(3.dp),
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
            modifier = Modifier.fillMaxWidth(),
            onClick = {
              Toast.makeText(context, "Not yet implemented", Toast.LENGTH_SHORT).show()
            }) {
              Icon(Icons.Default.Add, contentDescription = "Add")
              Text("Add links to other social media")
            }
        Row() {
          /* TODO row containing dynamic list of social media links */
        }
        Button(
            onClick = {
              val user =
                  User(
                      uid = Firebase.auth.currentUser?.uid!!,
                      email = Firebase.auth.currentUser?.email!!,
                      firstName = firstName,
                      lastName = lastName,
                      biography = bio,
                      followingAssociations = Association.emptyFirestoreReferenceList(),
                      interests = interests.filter { it.second.value }.map { it.first },
                      socials = emptyList<UserSocial>(),
                      profilePicture = "")
              uploadUser(user, userRepositoryFirestore, navigationAction, context)
              navigationAction.navigateTo(Screen.HOME)
            }) {
              Text("Continue")
            }
      }

  if (showOverlay1) {
    InterestOverlay(
        onDismiss = { showOverlay1 = false },
        onSave = { showOverlay1 = false },
        interests = interestsFlow)
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
      onFailure = { Log.e("AccountDetails", "Failed to upload user", it) })
}

class ComposableTestActivity : ComponentActivity() {

  val userRepositoryFirestore = UserRepositoryFirestore(Firebase.firestore)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      val navController = rememberNavController()
      val navigationActions = NavigationAction(navController)
      setContent {
        Surface(modifier = Modifier.fillMaxSize()) {
          AppTheme { AccountDetails(navigationActions, userRepositoryFirestore) }
        }
      }
    }
  }
}
