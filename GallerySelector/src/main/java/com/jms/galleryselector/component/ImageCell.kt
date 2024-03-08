package com.jms.galleryselector.component

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.jms.galleryselector.model.Gallery


/**
 *
 * Gallery cell
 */
@Composable
internal fun ImageCell(
    modifier: Modifier = Modifier,
    image: Gallery.Image
) {
    AsyncImage(
        modifier = modifier.fillMaxSize(),
        model = ImageRequest.Builder(LocalContext.current)
            .crossfade(true)
            .allowHardware(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .data(image.uri)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        filterQuality = FilterQuality.None,
    )
}
