package com.jms.imagePicker.component

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
    ImageCell(
        modifier = modifier,
        uri = mediaContent.uri
    )
}


@Composable
internal fun ImageCell(
    modifier: Modifier = Modifier,
    uri: Uri
) {
    AsyncImage(
        modifier = modifier,
        model = ImageRequest.Builder(LocalContext.current)
            .crossfade(true)
            .memoryCacheKey(uri.toString())
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCacheKey(uri.toString())
            .diskCachePolicy(CachePolicy.ENABLED)
            .data(uri)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun Preview_ImageCell() {
    ImageCell(uri = Uri.EMPTY)
}