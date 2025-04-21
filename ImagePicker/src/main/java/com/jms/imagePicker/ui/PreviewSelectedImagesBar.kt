package com.jms.imagePicker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jms.imagePicker.component.ImageCell
import com.jms.imagePicker.model.Gallery


@Composable
internal fun PreviewSelectedImagesBar(
    modifier: Modifier = Modifier,
    images: List<Gallery>
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        contentPadding = PaddingValues(5.dp)
    ) {
        items(images) {
            when (it) {
                is Gallery.Image -> {
                    ImageCell(
                        modifier = Modifier.size(50.dp),
                        image = it
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun Preview_PreviewSelectedImageBar() {
    PreviewSelectedImagesBar(images = emptyList())
}