package com.jms.imagePicker.component

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Scale
import com.jms.imagePicker.model.MediaContent
import kotlinx.coroutines.Dispatchers


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
    val targetPx = if (density.density >= 3f) rawPx / 2 else rawPx

    val request = remember(mediaContent.uri) {
        ImageRequest.Builder(context)
            .size(targetPx)
            .scale(Scale.FILL)
            .data(mediaContent.uri)
            .build()
    }

    AsyncImage(
        modifier = modifier,
        model = request,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        filterQuality = FilterQuality.Medium
    )
}