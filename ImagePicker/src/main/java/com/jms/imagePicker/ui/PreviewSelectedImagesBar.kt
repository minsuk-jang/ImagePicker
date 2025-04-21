package com.jms.imagePicker.ui

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jms.imagePicker.component.ImageCell
import java.util.Collections.emptyList


@Composable
internal fun PreviewSelectedImagesBar(
    modifier: Modifier = Modifier,
    uris: List<Uri>
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        contentPadding = PaddingValues(start = 10.dp, top = 5.dp, bottom = 5.dp, end = 10.dp)
    ) {
        items(
            items = uris,
            key = { it },
            contentType = { it }
        ) {
            Box(
                modifier = Modifier
                    .animateItem()
            ) {
                ImageCell(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    uri = it
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun Preview_PreviewSelectedImageBar() {
    PreviewSelectedImagesBar(uris = emptyList())
}