package com.jms.imagePicker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Scale
import com.jms.imagePicker.model.MediaContent
import java.util.Collections.emptyList


@Composable
internal fun ImagePreviewBar(
    modifier: Modifier = Modifier,
    mediaContents: List<MediaContent>,
    onClick: (MediaContent) -> Unit = {}
) {
    val context = LocalContext.current
    LazyRow(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(start = 10.dp, top = 1.dp, bottom = 5.dp, end = 10.dp)
    ) {
        items(
            items = mediaContents,
            key = { it.uri }
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clickable { onClick(it) }
                    .animateItem()
            ) {
                val request = remember(it.uri) {
                    ImageRequest.Builder(context)
                        .scale(Scale.FILL)
                        .memoryCachePolicy(CachePolicy.WRITE_ONLY)
                        .diskCachePolicy(CachePolicy.READ_ONLY)
                        .networkCachePolicy(CachePolicy.DISABLED)
                        .data(it.uri)
                        .build()
                }

                AsyncImage(
                    modifier = Modifier.matchParentSize(),
                    model = request,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )

                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                        .background(color = Color.DarkGray.copy(alpha = 0.7f), shape = CircleShape)
                ) {
                    Icon(
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.Center),
                        imageVector = Icons.Default.Close, contentDescription = "close",
                        tint = Color.White
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun Preview_ImagePreviewBar() {
    ImagePreviewBar(mediaContents = emptyList())
}