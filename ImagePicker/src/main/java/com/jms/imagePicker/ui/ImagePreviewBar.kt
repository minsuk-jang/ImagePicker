package com.jms.imagePicker.ui

import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jms.imagePicker.component.ImageCell
import java.util.Collections.emptyList


@Composable
internal fun ImagePreviewBar(
    modifier: Modifier = Modifier,
    uris: List<Uri>,
    onClick: (Uri) -> Unit = {}
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(start = 10.dp, top = 1.dp, bottom = 5.dp, end = 10.dp)
    ) {
        items(
            items = uris,
            key = { it }
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clickable { onClick(it) }
                    .animateItem()
            ) {
                ImageCell(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .align(Alignment.BottomStart),
                    uri = it
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
    ImagePreviewBar(uris = emptyList())
}