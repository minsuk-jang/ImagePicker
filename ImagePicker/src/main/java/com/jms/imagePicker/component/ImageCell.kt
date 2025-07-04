package com.jms.imagePicker.component

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.jms.imagePicker.data.API21MediaStoreThumbnailFetcher
import com.jms.imagePicker.data.API29MediaStoreThumbnailFetcher
import com.jms.imagePicker.model.MediaContent


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
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION_CODES.Q <= Build.VERSION.SDK_INT)
                    add(API29MediaStoreThumbnailFetcher.Factory(context = context))
                else
                    add(API21MediaStoreThumbnailFetcher.Factory(context = context))
            }
            .build()
    }

    val request = remember(mediaContent.uri) {
        ImageRequest.Builder(context)
            .size(rawPx)
            .scale(Scale.FILL)
            .data(mediaContent.uri)
            .build()
    }

    AsyncImage(
        modifier = modifier,
        imageLoader = imageLoader,
        model = request,
        contentDescription = null,
        contentScale = ContentScale.Crop,
    )
}