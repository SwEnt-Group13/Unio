package com.android.unio.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
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
import com.android.unio.model.user.UserSocial
import com.android.unio.ui.image.AsyncImageWrapper

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


@Composable
fun IconWithRemoveButton(
    userSocial: UserSocial,
    onRemove: () -> Unit,
    testTag: String
){
    val context = LocalContext.current
    Box(modifier = Modifier.size(50.dp).padding(8.dp)) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(userSocial.social.icon),
            contentDescription = userSocial.social.title,
            contentScale = ContentScale.Fit)
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription =
            context.getString(R.string.user_settings_content_description_remove_social),
            modifier =
            Modifier.align(Alignment.TopEnd).clickable { onRemove() }.testTag(testTag))
    }
}
