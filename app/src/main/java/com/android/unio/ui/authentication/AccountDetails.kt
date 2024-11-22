package com.android.unio.ui.authentication

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import com.android.unio.R
import com.android.unio.model.association.Association
import com.android.unio.model.event.Event
import com.android.unio.model.firestore.emptyFirestoreReferenceList
import com.android.unio.model.image.ImageRepository
import com.android.unio.model.strings.StoragePathsStrings
import com.android.unio.model.strings.test_tags.AccountDetailsTestTags
import com.android.unio.model.user.AccountDetailsError
import com.android.unio.model.user.Interest
import com.android.unio.model.user.User
import com.android.unio.model.user.UserSocial
import com.android.unio.model.user.UserViewModel
import com.android.unio.model.user.checkNewUser
import com.android.unio.ui.authentication.overlay.InterestOverlay
import com.android.unio.ui.authentication.overlay.SocialOverlay
import com.android.unio.ui.components.InterestInputChip
import com.android.unio.ui.components.ProfilePicturePicker
import com.android.unio.ui.components.SocialInputChip
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.theme.AppTypography
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun AccountDetailsScreen(
    navigationAction: NavigationAction,
    userViewModel: UserViewModel,
    imageRepository: ImageRepository
) {

  val context = LocalContext.current
  val userId = Firebase.auth.currentUser?.uid

  AccountDetailsContent(
      navigationAction,
      onCreateUser = { profilePictureUri, createUser ->
        if (profilePictureUri.value == Uri.EMPTY) {
          createUser("", userId!!)
        } else {
          val inputStream = context.contentResolver.openInputStream(profilePictureUri.value)
          imageRepository.uploadImage(
              inputStream!!,
              StoragePathsStrings.USER_IMAGES + userId,
              onSuccess = { imageUrl -> createUser(imageUrl, userId!!) },
              onFailure = { exception ->
                Log.e("AccountDetails", "Error uploading image: $exception")
                Toast.makeText(
                        context,
                        context.getString(R.string.account_details_image_upload_error),
                        Toast.LENGTH_SHORT)
                    .show()
              })
        }
      },
      onUploadUser = { user ->
        userViewModel.addUser(
            user,
            onSuccess = {
              Toast.makeText(
                      context,
                      context.getString(R.string.account_details_created_successfully),
                      Toast.LENGTH_SHORT)
                  .show()
              navigationAction.navigateTo(Screen.HOME)
            })
      })
}

/**
 * The [onCreateUser] function is called with the [profilePictureUri] and [createUser] lambda
 * function as parameters. [createUser] calls upon another lambda that uploads the user if all the
 * required fields are filled. This method hierarchy is necessary due to the fact that uploading an
 * image needs to know if the URI is empty or not before creating the User.
 */
@Composable
fun AccountDetailsContent(
    navigationAction: NavigationAction,
    onCreateUser: (MutableState<Uri>, (String, String) -> Unit) -> Unit,
    onUploadUser: (User) -> Unit,
) {
  var firstName: String by remember { mutableStateOf("") }
  var lastName: String by remember { mutableStateOf("") }
  var bio: String by remember { mutableStateOf("") }

  var isErrors by remember { mutableStateOf(mutableSetOf<AccountDetailsError>()) }

  val userInterestsFlow = remember {
    MutableStateFlow(Interest.entries.map { it to mutableStateOf(false) }.toList())
  }

  val userSocialsFlow = remember { MutableStateFlow(emptyList<UserSocial>().toMutableList()) }

  val interests by userInterestsFlow.collectAsState()
  val socials by userSocialsFlow.collectAsState()

  val profilePictureUri = remember { mutableStateOf<Uri>(Uri.EMPTY) }

  val context = LocalContext.current

  var showInterestsOverlay by remember { mutableStateOf(false) }
  var showSocialsOverlay by remember { mutableStateOf(false) }
  val scrollState = rememberScrollState()

  if (Firebase.auth.currentUser == null) {
    navigationAction.navigateTo(Screen.WELCOME)
    return
  }

  // the uri here is the path from the firebase/users to the location of the image in the storage
  val createUser: (String, String) -> Unit = { uri, userId ->
    val newUser =
        User(
            uid = userId,
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
      onUploadUser(newUser)
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

          UserTextFields(
              isErrors,
              firstName,
              lastName,
              bio,
              { firstName = it },
              { lastName = it },
              { bio = it })

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

                ProfilePicturePicker(
                    profilePictureUri,
                    { profilePictureUri.value = Uri.EMPTY },
                    AccountDetailsTestTags.PROFILE_PICTURE_ICON)
              }

          InterestButtonAndFlowRow(userInterestsFlow, { showInterestsOverlay = true })

          SocialButtonAndFlowRow(userSocialsFlow, { showSocialsOverlay = true })

          Button(
              modifier = Modifier.testTag(AccountDetailsTestTags.CONTINUE_BUTTON),
              onClick = { onCreateUser(profilePictureUri, createUser) }) {
                Text(context.getString(R.string.account_details_continue))
              }
        }

    if (showInterestsOverlay) {
      InterestOverlay(
          onDismiss = { showInterestsOverlay = false },
          onSave = { newInterests ->
            userInterestsFlow.value = newInterests
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
}

@Composable
private fun UserTextFields(
    isErrors: MutableSet<AccountDetailsError>,
    firstName: String,
    lastName: String,
    bio: String,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onBioChange: (String) -> Unit
) {
  val context = LocalContext.current
  val isFirstNameError = isErrors.contains(AccountDetailsError.EMPTY_FIRST_NAME)
  val isLastNameError = isErrors.contains(AccountDetailsError.EMPTY_LAST_NAME)

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
      onValueChange = onFirstNameChange,
      value = firstName)

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
      onValueChange = onLastNameChange,
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
      onValueChange = onBioChange,
      value = bio)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InterestButtonAndFlowRow(
    interestsFlow: MutableStateFlow<List<Pair<Interest, MutableState<Boolean>>>>,
    onShowInterests: () -> Unit,
) {
  val context = LocalContext.current

  val interests by interestsFlow.collectAsState()

  OutlinedButton(
      modifier = Modifier.fillMaxWidth().testTag(AccountDetailsTestTags.INTERESTS_BUTTON),
      onClick = onShowInterests) {
        Icon(
            Icons.Default.Add,
            contentDescription =
                context.getString(R.string.account_details_content_description_add))
        Text(context.getString(R.string.account_details_add_interests))
      }
  FlowRow {
    interests.forEach { pair ->
      if (pair.second.value) {
        InterestInputChip(pair = pair, testTag = AccountDetailsTestTags.INTERESTS_CHIP + pair.first.name)
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SocialButtonAndFlowRow(
    userSocialFlow: MutableStateFlow<MutableList<UserSocial>>,
    onShowSocials: () -> Unit
) {

  val context = LocalContext.current
  val socials by userSocialFlow.collectAsState()

  OutlinedButton(
      modifier = Modifier.fillMaxWidth().testTag(AccountDetailsTestTags.SOCIALS_BUTTON),
      onClick = onShowSocials) {
        Icon(
            Icons.Default.Add,
            contentDescription =
                context.getString(R.string.account_details_content_description_close))
        Text(context.getString(R.string.account_details_add_socials))
      }
  FlowRow(modifier = Modifier.fillMaxWidth()) {
    socials.forEachIndexed { index, userSocial ->
      SocialInputChip(
          userSocial = userSocial,
          onRemove = {
            userSocialFlow.value = userSocialFlow.value.toMutableList().apply { removeAt(index) }
          },
          testTag = AccountDetailsTestTags.SOCIALS_CHIP)
    }
  }
}
