package com.android.unio.ui.authentication

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.android.unio.R
import com.android.unio.model.association.Association
import com.android.unio.model.event.Event
import com.android.unio.model.firestore.emptyFirestoreReferenceList
import com.android.unio.model.image.ImageRepository
import com.android.unio.model.strings.test_tags.AccountDetailsTestTags
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
    userViewModel: UserViewModel,
    imageRepository: ImageRepository
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

  val profilePictureUri = remember { mutableStateOf<Uri>(Uri.EMPTY) }

  val pickMedia =
      rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
          profilePictureUri.value = uri
        }
      }

  val context = LocalContext.current

  var showInterestsOverlay by remember { mutableStateOf(false) }
  var showSocialsOverlay by remember { mutableStateOf(false) }
  val scrollState = rememberScrollState()

  if (Firebase.auth.currentUser == null) {
    navigationAction.navigateTo(Screen.WELCOME)
    return
  }

  val userId = Firebase.auth.currentUser?.uid

  val createUser = { uri: String ->
    val newUser =
        User(
            uid = userId!!,
            email = Firebase.auth.currentUser?.email!!,
            firstName = firstName,
            lastName = lastName,
            biography = bio,
            followedAssociations = Association.emptyFirestoreReferenceList(),
            joinedAssociations = Association.emptyFirestoreReferenceList(),
            savedEvents = Event.emptyFirestoreReferenceList(),
            interests = interests.filter { it.second.value }.map { it.first },
            socials = socials,
            profilePicture = uri)

    isErrors = checkNewUser(newUser)
    if (isErrors.isEmpty()) {
      userViewModel.addUser(
          newUser,
          onSuccess = {
            Toast.makeText(
                    context,
                    context.getString(R.string.account_details_created_successfully),
                    Toast.LENGTH_SHORT)
                .show()
            navigationAction.navigateTo(Screen.HOME)
          })
    }
  }

  Scaffold { padding ->
    Column(
        modifier =
            Modifier.padding(padding)
                .padding(vertical = 20.dp, horizontal = 40.dp)
                .verticalScroll(scrollState)
                .testTag(AccountDetailsTestTags.ACCOUNT_DETAILS),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
              text = context.getString(R.string.account_details_title),
              style = AppTypography.headlineSmall,
              modifier = Modifier.testTag(AccountDetailsTestTags.TITLE_TEXT))

          val isFirstNameError = isErrors.contains(AccountDetailsError.EMPTY_FIRST_NAME)
          OutlinedTextField(
              modifier =
                  Modifier.padding(4.dp)
                      .fillMaxWidth()
                      .testTag(AccountDetailsTestTags.FIRST_NAME_TEXT_FIELD),
              label = {
                Text(
                    context.getString(R.string.account_details_first_name),
                    modifier = Modifier.testTag(AccountDetailsTestTags.FIRST_NAME_TEXT))
              },
              isError = (isFirstNameError),
              supportingText = {
                if (isFirstNameError) {
                  Text(
                      context.getString(AccountDetailsError.EMPTY_FIRST_NAME.errorMessage),
                      modifier = Modifier.testTag(AccountDetailsTestTags.FIRST_NAME_ERROR_TEXT))
                }
              },
              onValueChange = { firstName = it },
              value = firstName)
          val isLastNameError = isErrors.contains(AccountDetailsError.EMPTY_LAST_NAME)
          OutlinedTextField(
              modifier =
                  Modifier.padding(4.dp)
                      .fillMaxWidth()
                      .testTag(AccountDetailsTestTags.LAST_NAME_TEXT_FIELD),
              label = {
                Text(
                    context.getString(R.string.account_details_last_name),
                    modifier = Modifier.testTag(AccountDetailsTestTags.LAST_NAME_TEXT))
              },
              isError = (isLastNameError),
              supportingText = {
                if (isLastNameError) {
                  Text(
                      context.getString(AccountDetailsError.EMPTY_LAST_NAME.errorMessage),
                      modifier = Modifier.testTag(AccountDetailsTestTags.LAST_NAME_ERROR_TEXT))
                }
              },
              onValueChange = { lastName = it },
              value = lastName)
          OutlinedTextField(
              modifier =
                  Modifier.padding(4.dp)
                      .fillMaxWidth()
                      .height(200.dp)
                      .testTag(AccountDetailsTestTags.BIOGRAPHY_TEXT_FIELD),
              label = {
                Text(
                    context.getString(R.string.account_details_bio),
                    modifier = Modifier.testTag(AccountDetailsTestTags.BIOGRAPHY_TEXT))
              },
              onValueChange = { bio = it },
              value = bio)

          Row(
              modifier = Modifier.fillMaxWidth().padding(8.dp),
              horizontalArrangement = Arrangement.SpaceEvenly,
              verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = context.getString(R.string.account_details_add_profile_picture),
                    modifier =
                        Modifier.widthIn(max = 140.dp)
                            .testTag(AccountDetailsTestTags.PROFILE_PICTURE_TEXT),
                    style = AppTypography.bodyLarge)

                if (profilePictureUri.value == Uri.EMPTY) {
                  Icon(
                      imageVector = Icons.Rounded.AccountCircle,
                      contentDescription = context.getString(R.string.account_details_content_description_add),
                      tint = primaryLight,
                      modifier =
                          Modifier.clickable {
                                pickMedia.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly))
                              }
                              .size(100.dp)
                              .testTag(AccountDetailsTestTags.PROFILE_PICTURE_ICON))
                } else {
                  ProfilePictureWithRemoveIcon(
                      profilePictureUri = profilePictureUri.value,
                      onRemove = { profilePictureUri.value = Uri.EMPTY })
                }
              }
          OutlinedButton(
              modifier = Modifier.fillMaxWidth().testTag(AccountDetailsTestTags.INTERESTS_BUTTON),
              onClick = { showInterestsOverlay = true }) {
                Icon(Icons.Default.Add, contentDescription = context.getString(R.string.account_details_content_description_add))
                Text(context.getString(R.string.account_details_add_interests))
              }
          FlowRow {
            interests.forEachIndexed { index, pair ->
              if (pair.second.value) {
                InputChip(
                    label = { Text(pair.first.name) },
                    onClick = {},
                    selected = pair.second.value,
                    modifier =
                        Modifier.padding(3.dp)
                            .testTag(AccountDetailsTestTags.INTERESTS_CHIP + "$index"),
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
              modifier = Modifier.fillMaxWidth().testTag(AccountDetailsTestTags.SOCIALS_BUTTON),
              onClick = { showSocialsOverlay = true }) {
                Icon(Icons.Default.Add, contentDescription = context.getString(R.string.account_details_content_description_close))
                Text(context.getString(R.string.account_details_add_socials))
              }
          FlowRow(modifier = Modifier.fillMaxWidth()) {
            socials.forEachIndexed { index, userSocial ->
              InputChip(
                  label = { Text(userSocial.social.name) },
                  onClick = {},
                  selected = true,
                  modifier =
                      Modifier.padding(3.dp)
                          .testTag(AccountDetailsTestTags.SOCIALS_CHIP + userSocial.social.title),
                  avatar = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = context.getString(R.string.account_details_content_description_close),
                        modifier =
                            Modifier.clickable {
                              userSocialsFlow.value =
                                  userSocialsFlow.value.toMutableList().apply { removeAt(index) }
                            })
                  })
            }
          }
          Button(
              modifier = Modifier.testTag(AccountDetailsTestTags.CONTINUE_BUTTON),
              onClick = {
                if (profilePictureUri.value == Uri.EMPTY) {
                  createUser("")
                } else {
                  val inputStream = context.contentResolver.openInputStream(profilePictureUri.value)

                imageRepository.uploadImage(
                    inputStream!!,
                    "images/users/${userId}",
                    onSuccess = createUser,
                    onFailure = { exception ->
                      Log.e("AccountDetails", "Error uploading image: $exception")
                      Toast.makeText(
                              context,
                              context.getString(R.string.account_details_image_upload_error),
                              Toast.LENGTH_SHORT)
                          .show()
                    })
              }
            }) {
              Text(context.getString(R.string.account_details_continue))
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

@Composable
private fun ProfilePictureWithRemoveIcon(
    profilePictureUri: Uri,
    onRemove: () -> Unit,
) {
  val context = LocalContext.current
  Box(modifier = Modifier.size(100.dp)) {
    Image(
        painter = rememberAsyncImagePainter(profilePictureUri),
        contentDescription = context.getString(R.string.account_details_content_description_pfp),
        contentScale = ContentScale.Crop,
        modifier = Modifier.aspectRatio(1f).clip(CircleShape))
    Icon(
        imageVector = Icons.Default.Close,
        contentDescription =
            context.getString(R.string.account_details_content_description_remove_pfp),
        modifier =
            Modifier.size(24.dp).align(Alignment.TopEnd).clickable { onRemove() }.padding(4.dp))
  }
}
