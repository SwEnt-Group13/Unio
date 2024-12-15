package com.android.unio.ui.event.overlay

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.android.unio.ui.image.AsyncImageWrapper

@Composable
fun PictureOverlay(
    picture: Uri,
    onDismiss: () -> Unit
) { // maybe directly put the AsyncImageWrapper composable as argument
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = true, dismissOnBackPress = true)
    ) {
        AsyncImageWrapper(
            imageUri = picture,
            contentDescription = "",
            filterQuality = FilterQuality.High,
            placeholderResourceId = 0,
            contentScale = ContentScale.Inside,
        )
    }
}