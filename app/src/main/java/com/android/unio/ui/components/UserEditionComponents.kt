package com.android.unio.ui.components

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.unio.R
import com.android.unio.model.user.Interest
import com.android.unio.model.user.UserSocial
import com.android.unio.ui.image.AsyncImageWrapper
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun ProfilePictureWithRemoveIcon(
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestButtonAndFlowRow(
    interestsFlow: MutableStateFlow<List<Pair<Interest, MutableState<Boolean>>>>,
    onShowInterests: () -> Unit,
    buttonTestTag: String,
    chipTestTag: String,

) {
    val context = LocalContext.current

    val interests by interestsFlow.collectAsState()

    OutlinedButton(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(buttonTestTag),
        onClick = onShowInterests) {
        Icon(
            Icons.Default.Add,
            contentDescription =
            context.getString(R.string.account_details_content_description_add))
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
                    Modifier
                        .padding(3.dp)
                        .testTag(chipTestTag + "$index"),
                    avatar = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Add",
                            modifier = Modifier.clickable { pair.second.value = !pair.second.value })
                    })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SocialButtonAndFlowRow(
    userSocialFlow: MutableStateFlow<MutableList<UserSocial>>,
    onShowSocials: () -> Unit,
    buttonTestTag: String,
    chipTestTag: String,



){

    val context = LocalContext.current
    val socials by userSocialFlow.collectAsState()


    OutlinedButton(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(buttonTestTag),
        onClick = onShowSocials) {
        Icon(
            Icons.Default.Add,
            contentDescription =
            context.getString(R.string.account_details_content_description_close))
        Text(context.getString(R.string.account_details_add_socials))
    }
    FlowRow(modifier = Modifier.fillMaxWidth()) {
        socials.forEachIndexed { index, userSocial ->
            InputChip(
                label = { Text(userSocial.social.name) },
                onClick = {},
                selected = true,
                modifier =
                Modifier
                    .padding(3.dp)
                    .testTag(chipTestTag + userSocial.social.title),
                avatar = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription =
                        context.getString(R.string.account_details_content_description_close),
                        modifier =
                        Modifier.clickable {
                            userSocialFlow.value =
                                userSocialFlow.value.toMutableList().apply { removeAt(index) }
                        })
                })
        }
    }
}