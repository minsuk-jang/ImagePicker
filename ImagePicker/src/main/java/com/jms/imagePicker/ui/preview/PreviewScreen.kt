package com.jms.imagePicker.ui.preview

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.jms.imagePicker.model.MediaContent
import com.jms.imagePicker.ui.ImagePickerViewModel


@Composable
internal fun PreviewScreen(
    modifier: Modifier = Modifier,
    viewModel: ImagePickerViewModel,
    onBack: () -> Unit = {},
    initializeFirstVisibleItemIndex: Int = 0
) {
    val mediaContents = viewModel.mediaContents.collectAsLazyPagingItems()

    BackHandler {
        onBack()
    }

    PreviewContent(
        modifier = modifier,
        mediaContents = mediaContents,
        onBack = onBack,
        initializeFirstVisibleItemIndex = initializeFirstVisibleItemIndex,
        onSelect = {

        }
    )
}


@Composable
private fun PreviewContent(
    modifier: Modifier = Modifier,
    mediaContents: LazyPagingItems<MediaContent>,
    initializeFirstVisibleItemIndex: Int = 0,
    onBack: () -> Unit = {},
    onSelect: () -> Unit = {}
) {
    val context = LocalContext.current

    val listState = rememberPagerState(
        initialPage = initializeFirstVisibleItemIndex
    ) { mediaContents.itemCount }

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
            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = listState,
            ) {
                val image = mediaContents[it]

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.Black)
                ) {
                    image?.let {
                        AsyncImage(
                            modifier = Modifier
                                .wrapContentSize()
                                .align(Alignment.Center),
                            model = ImageRequest.Builder(context)
                                .data(it.uri)
                                .build(),
                            contentDescription = "content"
                        )
                    }
                }
            }
        }
    }
}