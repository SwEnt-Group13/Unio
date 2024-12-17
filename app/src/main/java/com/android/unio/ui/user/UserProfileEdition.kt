package com.android.unio.ui.user

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import com.android.unio.R
import com.android.unio.model.image.ImageViewModel
import com.android.unio.model.strings.StoragePathsStrings
import com.android.unio.model.strings.test_tags.user.UserEditionTestTags
import com.android.unio.model.user.AccountDetailsError
import com.android.unio.model.user.ImageUriType
import com.android.unio.model.user.Interest
import com.android.unio.model.user.User
import com.android.unio.model.user.UserSocial
import com.android.unio.model.user.UserViewModel
import com.android.unio.model.user.checkImageUri
import com.android.unio.model.user.checkNewUser
import com.android.unio.model.utils.NetworkUtils
import com.android.unio.model.utils.TextLength
import com.android.unio.model.utils.Utils
import com.android.unio.ui.authentication.overlay.InterestOverlay
import com.android.unio.ui.authentication.overlay.SocialOverlay
import com.android.unio.ui.components.InterestInputChip
import com.android.unio.ui.components.ProfilePicturePicker
import com.android.unio.ui.components.SocialInputChip
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.theme.errorContainerDarkMediumContrast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * User edit screen that allows the user to edit their profile. This composable simply calls
 * [UserProfileEditionScreenScaffold] with the necessary parameters.
 *
 * @param userViewModel The [UserViewModel] for the user.
 * @param imageViewModel The [ImageViewModel] for the profile picture.
 * @param navigationAction The [NavigationAction] to navigate to different screens.
 */
@Composable
fun UserProfileEditionScreen(
    userViewModel: UserViewModel,
    imageViewModel: ImageViewModel,
    navigationAction: NavigationAction
) {

  val context = LocalContext.current

  val user by userViewModel.user.collectAsState()
  val userId = user!!.uid

  UserProfileEditionScreenScaffold(
      user = user!!,
      onDiscardChanges = { navigationAction.goBack() },
      onModifyUser = { profilePictureUri, createUser ->
        val uriType = checkImageUri(profilePictureUri.value.toString())

        when (uriType) {
          ImageUriType.EMPTY -> createUser("") // create a user with an empty URI
          ImageUriType.REMOTE ->
              createUser(user!!.profilePicture) // create a user with the same URI
          ImageUriType.LOCAL -> { // create a user with a new URI and upload the image to storage
            val inputStream = context.contentResolver.openInputStream(profilePictureUri.value)
            imageViewModel.uploadImage(
                inputStream!!,
                StoragePathsStrings.USER_IMAGES + userId,
                onSuccess = { imageUrl -> createUser(imageUrl) },
                onFailure = {
                  Toast.makeText(
                          context,
                          context.getString(R.string.account_details_image_upload_error),
                          Toast.LENGTH_SHORT)
                      .show()
                })
          }
        }
      },
      onUploadUserOnline = { modifiedUser ->
        userViewModel.addUser(
            modifiedUser,
            onSuccess = {
              Toast.makeText(
                      context,
                      context.getString(R.string.user_edition_modified_successfully),
                      Toast.LENGTH_SHORT)
                  .show()
              navigationAction.goBack()
            })
      },
      onUploadUserOffline = { modifiedUser ->
        Toast.makeText(
                context,
                context.getString(R.string.user_edition_modified_offline),
                Toast.LENGTH_LONG)
            .show()
        userViewModel.addUser(
            modifiedUser,
            onSuccess = {
              Toast.makeText(
                      context,
                      context.getString(R.string.user_edition_modified_successfully),
                      Toast.LENGTH_SHORT)
                  .show()
              navigationAction.goBack()
            })
      },
      onDeleteUser = { uid ->
        CoroutineScope(Dispatchers.Main).launch {
          userViewModel.deleteUser(
              uid,
              deleteWithProfilePicture = user!!.profilePicture != Uri.EMPTY.toString(),
              onSuccess = {
                Toast.makeText(
                        context,
                        context.getString(R.string.user_edition_delete_user_success),
                        Toast.LENGTH_SHORT)
                    .show()
                navigationAction.navigateTo(Screen.WELCOME)
              },
              onFailure = {
                Toast.makeText(
                        context,
                        context.getString(R.string.user_edition_delete_user_failure),
                        Toast.LENGTH_SHORT)
                    .show()
              })
        }
      })
}

/**
 * A scaffold that contains content of the user profile edition screen.
 *
 * @param user The user to edit.
 * @param onDiscardChanges The function to call when the user wants to discard the changes.
 * @param onModifyUser The function to call when the user wants to modify their profile.
 * @param onUploadUserOnline The function to call when the user wants to upload their profile
 *   online.
 * @param onUploadUserOffline The function to call when the user wants to upload their profile
 *   offline.
 * @param onDeleteUser The function to call when the user wants to delete their profile.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileEditionScreenScaffold(
    user: User,
    onDiscardChanges: () -> Unit,
    onModifyUser: (MutableState<Uri>, (String) -> Unit) -> Unit,
    onUploadUserOnline: (User) -> Unit,
    onUploadUserOffline: (User) -> Unit,
    onDeleteUser: (User) -> Unit
) {

  val context = LocalContext.current

  var firstName: String by remember { mutableStateOf(user.firstName) }
  var lastName: String by remember { mutableStateOf(user.lastName) }
  var bio: String by remember { mutableStateOf(user.biography) }

  var isErrors by remember { mutableStateOf(mutableSetOf<AccountDetailsError>()) }

  val userInterestsFlow = remember {
    MutableStateFlow(Interest.entries.map { it to mutableStateOf(user.interests.contains(it)) })
  }

  val userSocialsFlow = remember { MutableStateFlow(user.socials.toMutableList()) }

  val interests by userInterestsFlow.collectAsState()
  val socials by userSocialsFlow.collectAsState()

  // This is the local uri of the new profile picture stored locally
  // But if it's the first time entering the page the uri will be the one from firebase
  // and therefore cannot be opened in a input stream in the onModifyUser function
  // Hence we must check whether the user has changed his profile picture or not to get a local URI.
  val profilePictureUri = remember { mutableStateOf(user.profilePicture.toUri()) }

  var showInterestsOverlay by remember { mutableStateOf(false) }
  var showSocialsOverlay by remember { mutableStateOf(false) }
  var showDeleteUserPrompt by remember { mutableStateOf(false) }

  val scrollState = rememberScrollState()

  /**
   * uid, email, followedAssociations, joinedAssociations and savedEvents will not be modified and
   * simply be copied from the user.
   */
  val createUser: (String) -> Unit = { uri ->
    val hasInternet = NetworkUtils.checkInternetConnection(context)
    val newUser =
        User(
            uid = user.uid,
            email = user.email,
            firstName = firstName,
            lastName = lastName,
            biography = bio,
            followedAssociations = user.followedAssociations,
            joinedAssociations = user.joinedAssociations,
            savedEvents = user.savedEvents,
            interests = interests.filter { it.second.value }.map { it.first },
            socials = socials,
            profilePicture = uri)

    isErrors = checkNewUser(newUser)
    if (isErrors.isEmpty()) {
      if (hasInternet) {
        onUploadUserOnline(newUser)
      } else {
        onUploadUserOffline(newUser)
      }
    }
  }

  Scaffold(
      modifier = Modifier.fillMaxWidth(),
      topBar = {
        TopAppBar(
            title = {
              Text(
                  context.getString(R.string.user_edition_discard_changes),
                  modifier = Modifier.testTag(UserEditionTestTags.DISCARD_TEXT))
            },
            navigationIcon = {
              IconButton(
                  onClick = onDiscardChanges,
                  modifier = Modifier.testTag(UserEditionTestTags.DISCARD_BUTTON)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back arrow")
                  }
            })
      },
  ) { padding ->
    Column(
        modifier =
            Modifier.padding(padding).fillMaxWidth().padding(40.dp).verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally) {
          ProfilePicturePicker(
              profilePictureUri,
              { profilePictureUri.value = Uri.EMPTY },
              UserEditionTestTags.PROFILE_PICTURE_ICON)

          EditUserTextFields(
              isErrors = isErrors,
              firstName = firstName,
              lastName = lastName,
              bio = bio,
              onFirstNameChange = { firstName = it },
              onLastNameChange = { lastName = it },
              onBioChange = { bio = it },
          )

          SocialButtonAndFlowRow(
              userSocialFlow = userSocialsFlow, onShowSocials = { showSocialsOverlay = true })

          InterestButtonAndFlowRow(
              interestsFlow = userInterestsFlow, onShowInterests = { showInterestsOverlay = true })

          Button(
              onClick = { onModifyUser(profilePictureUri, createUser) },
              modifier = Modifier.testTag(UserEditionTestTags.SAVE_BUTTON)) {
                Text(context.getString(R.string.user_edition_save_changes))
              }

          Button(
              onClick = {
                if (NetworkUtils.checkInternetConnection(context)) {
                  showDeleteUserPrompt = true
                } else {
                  Toast.makeText(
                          context,
                          context.getString(R.string.user_edition_delete_user_offline),
                          Toast.LENGTH_SHORT)
                      .show()
                }
              },
              modifier = Modifier.testTag(UserEditionTestTags.DELETE_BUTTON).padding(10.dp),
              colors =
                  ButtonDefaults.buttonColors(containerColor = errorContainerDarkMediumContrast),
          ) {
            Text(context.getString(R.string.user_edition_delete_user))
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

    if (showDeleteUserPrompt) {
      UserDeletePrompt(
          onDismiss = { showDeleteUserPrompt = false }, onConfirmDelete = { onDeleteUser(user) })
    }
  }
}

/**
 * Edit user text fields for the user to edit their profile.
 *
 * @param isErrors The set of errors that the user has made.
 * @param firstName The first name of the user.
 * @param lastName The last name of the user.
 * @param bio The biography of the user.
 * @param onFirstNameChange The function to call when the user changes their first name.
 * @param onLastNameChange The function to call when the user changes their last name.
 * @param onBioChange The function to call when the user changes their biography.
 */
@Composable
private fun EditUserTextFields(
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
      modifier = Modifier.padding(4.dp).testTag(UserEditionTestTags.FIRST_NAME_TEXT_FIELD),
      label = {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
              context.getString(R.string.user_edition_first_name),
              modifier = Modifier.testTag(UserEditionTestTags.FIRST_NAME_TEXT))

          if (Utils.checkInputLengthIsClose(firstName, TextLength.SMALL)) {
            Text(
                text = "${firstName.length}/${TextLength.SMALL.length}",
                modifier = Modifier.testTag(UserEditionTestTags.FIRST_NAME_CHARACTER_COUNTER))
          }
        }
      },
      isError = (isFirstNameError),
      supportingText = {
        if (isFirstNameError) {
          Text(
              context.getString(AccountDetailsError.EMPTY_FIRST_NAME.errorMessage),
              modifier = Modifier.testTag(UserEditionTestTags.FIRST_NAME_ERROR_TEXT).padding(4.dp))
        }
      },
      onValueChange = {
        if (Utils.checkInputLength(it, TextLength.SMALL)) {
          onFirstNameChange(it)
        }
      },
      value = firstName)

  OutlinedTextField(
      modifier = Modifier.padding(4.dp).testTag(UserEditionTestTags.LAST_NAME_TEXT_FIELD),
      label = {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
              context.getString(R.string.user_edition_last_name),
              modifier = Modifier.testTag(UserEditionTestTags.LAST_NAME_TEXT).padding(4.dp))

          if (Utils.checkInputLengthIsClose(lastName, TextLength.SMALL)) {
            Text(
                text = "${lastName.length}/${TextLength.SMALL.length}",
                modifier = Modifier.testTag(UserEditionTestTags.LAST_NAME_CHARACTER_COUNTER))
          }
        }
      },
      isError = (isLastNameError),
      supportingText = {
        if (isLastNameError) {
          Text(
              context.getString(AccountDetailsError.EMPTY_LAST_NAME.errorMessage),
              modifier = Modifier.testTag(UserEditionTestTags.LAST_NAME_ERROR_TEXT))
        }
      },
      onValueChange = {
        if (Utils.checkInputLength(it, TextLength.SMALL)) {
          onLastNameChange(it)
        }
      },
      value = lastName)

  OutlinedTextField(
      modifier =
          Modifier.padding(4.dp)
              .fillMaxWidth()
              .height(200.dp)
              .testTag(UserEditionTestTags.BIOGRAPHY_TEXT_FIELD),
      label = {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
              context.getString(R.string.user_edition_bio),
              modifier = Modifier.testTag(UserEditionTestTags.BIOGRAPHY_TEXT).padding(4.dp))

          if (Utils.checkInputLengthIsClose(bio, TextLength.LARGE)) {
            Text(
                text = "${bio.length}/${TextLength.LARGE.length}",
                modifier = Modifier.testTag(UserEditionTestTags.BIOGRAPHY_CHARACTER_COUNTER))
          }
        }
      },
      onValueChange = onBioChange,
      value = bio)
}

/**
 * The [InterestButtonAndFlowRow] composable contains the button to add interests and display the
 * row of interests that the user has selected.
 *
 * @param interestsFlow The flow of interests coupled with a mutable state of whether the user has
 *   selected them.
 * @param onShowInterests The function to call when the user wants to show the interests overlay.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InterestButtonAndFlowRow(
    interestsFlow: MutableStateFlow<List<Pair<Interest, MutableState<Boolean>>>>,
    onShowInterests: () -> Unit,
) {
  val context = LocalContext.current

  val interests by interestsFlow.collectAsState()

  OutlinedButton(
      modifier = Modifier.fillMaxWidth().testTag(UserEditionTestTags.INTERESTS_BUTTON),
      onClick = onShowInterests) {
        Icon(
            Icons.Default.Add,
            contentDescription =
                context.getString(R.string.account_details_content_description_add))
        Text(context.getString(R.string.user_edition_edit_interests))
      }
  FlowRow {
    interests.forEach { pair ->
      if (pair.second.value) {
        InterestInputChip(pair, testTag = UserEditionTestTags.INTERESTS_CHIP + pair.first.name)
      }
    }
  }
}

/**
 * The [SocialButtonAndFlowRow] composable contains the button to add socials and display the row of
 * socials that the user has selected.
 *
 * @param userSocialFlow The flow of socials that the user has selected.
 * @param onShowSocials The function to call when the user wants to show the socials overlay.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SocialButtonAndFlowRow(
    userSocialFlow: MutableStateFlow<MutableList<UserSocial>>,
    onShowSocials: () -> Unit,
) {

  val context = LocalContext.current
  val socials by userSocialFlow.collectAsState()

  OutlinedButton(
      modifier = Modifier.fillMaxWidth().testTag(UserEditionTestTags.SOCIALS_BUTTON),
      onClick = onShowSocials) {
        Icon(
            Icons.Default.Add,
            contentDescription =
                context.getString(R.string.account_details_content_description_close))
        Text(context.getString(R.string.user_edition_edit_socials))
      }

  FlowRow(modifier = Modifier.fillMaxWidth().padding(3.dp)) {
    socials.forEachIndexed { index, userSocial ->
      SocialInputChip(
          userSocial,
          onRemove = {
            userSocialFlow.value = userSocialFlow.value.toMutableList().apply { removeAt(index) }
          },
          testTag = UserEditionTestTags.SOCIALS_CHIP)
    }
  }
}

/**
 * The [UserDeletePrompt] composable contains the dialog that prompts the user to confirm the
 * deletion of their account.
 *
 * @param onDismiss The function to call when the user wants to dismiss the dialog.
 * @param onConfirmDelete The function to call when the user wants to confirm the deletion of their
 *   account.
 */
@Composable
fun UserDeletePrompt(
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit,
) {
  val context = LocalContext.current

  Dialog(
      onDismissRequest = onDismiss,
      properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(20.dp).testTag("")) {
              Column(
                  modifier = Modifier.padding(16.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = context.getString(R.string.user_edition_delete_user_confirmation),
                        textAlign = TextAlign.Center)

                    Button(
                        modifier = Modifier.testTag(UserEditionTestTags.DELETE_CONFIRMATION),
                        onClick = onConfirmDelete,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = errorContainerDarkMediumContrast)) {
                          Text(context.getString(R.string.user_edition_delete_user_confirm))
                        }
                  }
            }
      }
}
