package com.jms.imagePicker.component

import android.os.Build
import androidx.compose.foundation.lazy.layout.LazyLayoutPrefetchState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Scale
import com.jms.imagePicker.data.API21MediaStoreThumbnailFetcher
import com.jms.imagePicker.data.API29MediaStoreThumbnailFetcher
import com.jms.imagePicker.model.MediaContent
import com.jms.imagePicker.ui.LocalImageLoader


/**
 *
 * Gallery cell
 */
@Composable
internal fun ImageCell(
    modifier: Modifier = Modifier,
    cellDp: Dp,
    mediaContent: MediaContent
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val rawPx = with(density) { cellDp.roundToPx() }


    val request = remember(mediaContent.uri) {
        ImageRequest.Builder(context)
            .size(rawPx)
            .scale(Scale.FILL)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .data(mediaContent.uri)
            .build()
    }

    AsyncImage(
        modifier = modifier,
        imageLoader = LocalImageLoader.current,
        placeholder = ColorPainter(color = Color.LightGray),
        model = request,
        contentDescription = null,
        contentScale = ContentScale.Crop,
    )
}