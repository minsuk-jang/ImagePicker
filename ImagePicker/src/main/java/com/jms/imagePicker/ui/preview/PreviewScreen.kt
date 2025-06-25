package com.jms.imagePicker.ui.preview

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListPrefetchStrategy
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.jms.imagePicker.component.ImageCell
import com.jms.imagePicker.model.Gallery
import com.jms.imagePicker.ui.ImagePickerViewModel
import kotlin.math.log


@Composable
internal fun PreviewScreen(
    modifier: Modifier = Modifier,
    viewModel: ImagePickerViewModel,
    onBack: () -> Unit = {},
    initializeFirstVisibleItemIndex: Int = 0
) {
    val images = viewModel.images.collectAsLazyPagingItems()

    PreviewContent(
        modifier = modifier,
        images = images,
        onBack = onBack,
        initializeFirstVisibleItemIndex = initializeFirstVisibleItemIndex,
        onSelect = {

        }
    )
}


@Composable
private fun PreviewContent(
    modifier: Modifier = Modifier,
    images: LazyPagingItems<Gallery.Image>,
    initializeFirstVisibleItemIndex: Int = 0,
    onBack: () -> Unit = {},
    onSelect: () -> Unit = {}
) {

    Log.e("jms8732", "index: $initializeFirstVisibleItemIndex")
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = initializeFirstVisibleItemIndex
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.Black)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                modifier = Modifier.align(Alignment.CenterStart),
                onClick = { onBack() }
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "back",
                    tint = Color.White
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyRow(
                modifier = Modifier.fillMaxSize(),
                state = listState,
            ) {
                items(
                    images.itemCount,
                    key = images.itemKey { it.uri }
                ) {
                    images[it]?.let {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = Color.Black)
                        ) {
                            AsyncImage(
                                modifier = modifier
                                    .wrapContentSize(),
                                model = ImageRequest.Builder(LocalContext.current)
                                    .crossfade(true)
                                    .memoryCacheKey(it.uri.toString())
                                    .memoryCachePolicy(CachePolicy.ENABLED)
                                    .diskCacheKey(it.uri.toString())
                                    .diskCachePolicy(CachePolicy.ENABLED)
                                    .data(it.uri)
                                    .build(),
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    }
}