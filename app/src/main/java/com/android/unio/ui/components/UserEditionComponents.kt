package com.android.unio.ui.components

import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ModalBottomSheetLayout

import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.android.unio.R
import com.android.unio.model.user.Interest
import com.android.unio.model.user.UserSocial
import com.android.unio.ui.image.AsyncImageWrapper
import com.android.unio.ui.theme.primaryLight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
private fun ProfilePictureWithRemoveIcon(
    profilePictureUri: Uri,
    onRemove: () -> Unit,
) {
  val context = LocalContext.current
  Box(modifier = Modifier.size(100.dp)) {
    AsyncImageWrapper(
        imageUri = profilePictureUri,
        contentDescription = context.getString(R.string.account_details_content_description_pfp),
        contentScale = ContentScale.Crop,
        modifier = Modifier.aspectRatio(1f).clip(CircleShape),
        filterQuality = FilterQuality.Medium,
        placeholderResourceId = 0 // to have no placeholder
        )
    Icon(
        imageVector = Icons.Default.Close,
        contentDescription =
            context.getString(R.string.account_details_content_description_remove_pfp),
        modifier =
            Modifier.size(24.dp).align(Alignment.TopEnd).clickable { onRemove() }.padding(4.dp))
  }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePicturePicker(
    profilePictureUri: MutableState<Uri>,
    onProfilePictureUriChange: () -> Unit,
    testTag: String
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded  = true)
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }

    if (profilePictureUri.value == Uri.EMPTY) {
        Icon(
            imageVector = Icons.Rounded.AccountCircle,
            contentDescription = context.getString(R.string.account_details_content_description_add),
            tint = primaryLight,
            modifier = Modifier
                .clickable {
                    showSheet = true
                }
                .size(100.dp)
                .testTag(testTag)
        )
    } else {
        ProfilePictureWithRemoveIcon(
            profilePictureUri = profilePictureUri.value,
            onRemove = onProfilePictureUriChange
        )
    }

    if (showSheet) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { scope.launch { sheetState.hide(); showSheet=false } },
            content = {
                PictureSelectionTool(
                    maxPictures = 1,
                    allowGallery = true,
                    allowCamera = true,
                    onValidate = { uris ->
                        if (uris.isNotEmpty()) {
                            profilePictureUri.value = uris.first() // Use the first selected as the profile picture
                        }
                        scope.launch { sheetState.hide() }
                        showSheet = false
                    },
                    onCancel = {
                        scope.launch { sheetState.hide() }
                        showSheet = false
                    }
                )
            }
        )
    }
}



@Composable
fun InterestInputChip(pair: Pair<Interest, MutableState<Boolean>>, testTag: String) {
  val context = LocalContext.current

  InputChip(
      label = { Text(context.getString(pair.first.title)) },
      onClick = {},
      selected = pair.second.value,
      modifier = Modifier.padding(3.dp).testTag(testTag),
      avatar = {
        Icon(
            Icons.Default.Close,
            contentDescription = "Add",
            modifier = Modifier.clickable { pair.second.value = !pair.second.value })
      })
}

@Composable
fun SocialInputChip(userSocial: UserSocial, onRemove: () -> Unit, testTag: String) {
  val context = LocalContext.current

  InputChip(
      label = { Text(userSocial.social.title) },
      onClick = {},
      selected = true,
      modifier = Modifier.testTag(testTag + userSocial.social.title),
      avatar = {
        Image(
            modifier = Modifier.size(24.dp),
            painter = painterResource(userSocial.social.icon),
            contentDescription = userSocial.social.title)
      },
      trailingIcon = {
        Icon(
            Icons.Default.Close,
            contentDescription =
                context.getString(R.string.account_details_content_description_close),
            modifier = Modifier.clickable { onRemove() })
      })
}
