package com.jms.imagePicker.component

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.jms.imagePicker.model.MediaContent


/**
 *
 * Gallery cell
 */
@Composable
internal fun ImageCell(
    modifier: Modifier = Modifier,
    mediaContent: MediaContent
) {
    AsyncImage(
        modifier = modifier,
        model = ImageRequest.Builder(LocalContext.current)
            .crossfade(true)
            .memoryCacheKey(mediaContent.uri.toString())
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCacheKey(mediaContent.uri.toString())
            .diskCachePolicy(CachePolicy.ENABLED)
            .data(mediaContent.uri)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop
    )
}