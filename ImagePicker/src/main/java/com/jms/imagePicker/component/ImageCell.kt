package com.jms.imagePicker.component

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.memory.MemoryCache
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
    val context = LocalContext.current
    val request = remember(mediaContent.uri) {
        ImageRequest.Builder(context)
            //.crossfade(true)
            .size(200)
            .data(mediaContent.uri)
            .build()
    }

    AsyncImage(
        modifier = modifier,
        model = request,
        contentDescription = null,
        contentScale = ContentScale.Crop
    )
}