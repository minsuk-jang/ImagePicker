package com.jms.imagePicker.component

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.jms.imagePicker.R
import com.jms.imagePicker.model.Gallery


/**
 *
 * Gallery cell
 */
@Composable
internal fun ImageCell(
    modifier: Modifier = Modifier,
    image: Gallery.Image,
    onNavigateToPreview: (Gallery) -> Unit = {}
) {
    Box {
        ImageCell(
            modifier = modifier,
            uri = image.uri
        )
        Row(
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            Spacer(modifier = Modifier.width(5.dp))
            Column {
                Icon(
                    modifier = Modifier.clickable {
                        onNavigateToPreview(image)
                    },
                    painter = painterResource(id = R.drawable.open_in_fill),
                    contentDescription = "open_in_fill"
                )
                Spacer(modifier = Modifier.width(5.dp))
            }
        }
    }
}


@Composable
internal fun ImageCell(
    modifier: Modifier = Modifier,
    uri: Uri
) {
    Box {
        AsyncImage(
            modifier = modifier,
            model = ImageRequest.Builder(LocalContext.current)
                .crossfade(true)
                .allowHardware(true)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .data(uri)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            filterQuality = FilterQuality.None,
        )
    }
}

@Composable
private fun Preview_ImageCell() {
    ImageCell(uri = Uri.EMPTY)
}