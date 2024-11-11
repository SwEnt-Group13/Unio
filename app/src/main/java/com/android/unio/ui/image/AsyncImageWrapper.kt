package com.android.unio.ui.image

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.android.unio.R

@Composable
fun AsyncImageWrapper(
    imageUri: Uri,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.None
) {
    val noImageResId = R.drawable.no_picture_found
    val imageRequest =
        ImageRequest.Builder(LocalContext.current)
            .data(imageUri)
            .memoryCacheKey(imageUri.toString())
            .diskCacheKey(imageUri.toString())
            .placeholder(noImageResId)
            .error(noImageResId)
            .fallback(noImageResId)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .build()

    AsyncImage(
        model = imageRequest,
        contentDescription =
        contentDescription,
        modifier =
        modifier,
        contentScale = contentScale
    )


}