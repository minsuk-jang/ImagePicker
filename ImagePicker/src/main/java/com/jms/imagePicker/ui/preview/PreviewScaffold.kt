package com.jms.imagePicker.ui.preview

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.platform.LocalContext
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.jms.imagePicker.model.MediaContent
import com.jms.imagePicker.ui.ImagePickerViewModel
import com.jms.imagePicker.ui.picker.ImagePickerNavHostState
import com.jms.imagePicker.ui.action.PreviewActions


@Composable
internal fun PreviewScaffold(
    viewModel: ImagePickerViewModel,
    state: ImagePickerNavHostState,
    onBack: () -> Unit = {},
    initializeFirstVisibleItemIndex: Int = 0,
    content: @Composable BoxScope.(PreviewActions, MediaContent) -> Unit = { _, _ -> }
) {
    val mediaContents = viewModel.mediaContents.collectAsLazyPagingItems()

    BackHandler {
        onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black)
    ) {
        if (mediaContents.loadState.refresh is LoadState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.LightGray
            )

            return
        }

        val handler: PreviewActions = remember(viewModel, state) {
            object : PreviewActions {
                override fun onBack() {
                    onBack()
                }

                override fun onClick(mediaContent: MediaContent) {
                    viewModel.select(uri = mediaContent.uri, max = state.max)
                }
            }
        }

        PreviewContent(
            mediaContents = mediaContents,
            initializeFirstVisibleItemIndex = initializeFirstVisibleItemIndex,
        ) {
            content(handler, it)
        }
    }
}


@Composable
private fun BoxScope.PreviewContent(
    mediaContents: LazyPagingItems<MediaContent>,
    initializeFirstVisibleItemIndex: Int = 0,
    content: @Composable BoxScope.(MediaContent) -> Unit = {}
) {
    val context = LocalContext.current

    val listState = rememberPagerState(
        initialPage = initializeFirstVisibleItemIndex
    ) { mediaContents.itemCount }

    HorizontalPager(
        modifier = Modifier.fillMaxSize(),
        state = listState,
    ) {
        mediaContents[it]?.let {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                AsyncImage(
                    modifier = Modifier
                        .matchParentSize()
                        .align(Alignment.Center),
                    model = ImageRequest.Builder(context)
                        .data(it.uri)
                        .build(),
                    contentDescription = "content",
                    filterQuality = FilterQuality.High,
                )
            }
        }
    }

    mediaContents[listState.currentPage]?.let {
        content(it)
    }
}