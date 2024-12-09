package com.android.unio.ui.image

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultFilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.android.unio.R

/**
 * A wrapper around AsyncImage composable that builds the imageRequest before passing it to
 * AsyncImage to load images asynchronously
 *
 * @param imageUri : Uri of the image
 * @param contentDescription : Content description (accessibility purposes)
 * @param modifier : the modifier to be applied on AsyncImage composable
 * @param contentScale : How the image is scaled
 * @param placeholderResourceId : Resource id of the placeholder (0 for a blank image)
 * @param filterQuality : type of filtering algorithm. None is the fastest, High the slowest
 */
@Composable
fun AsyncImageWrapper(
    imageUri: Uri,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.None,
    placeholderResourceId: Int = R.drawable.no_picture_found,
    filterQuality: FilterQuality = DefaultFilterQuality,
) {
  val imageRequest =
      ImageRequest.Builder(LocalContext.current)
          .data(imageUri)
          .memoryCacheKey(imageUri.toString())
          .diskCacheKey(imageUri.toString())
          .placeholder(placeholderResourceId)
          .error(placeholderResourceId)
          .fallback(placeholderResourceId)
          .diskCachePolicy(CachePolicy.ENABLED)
          .memoryCachePolicy(CachePolicy.ENABLED)
          .crossfade(true)
          .build()

  AsyncImage(
      model = imageRequest,
      contentDescription = contentDescription,
      modifier = modifier,
      contentScale = contentScale,
      filterQuality = filterQuality)
}
