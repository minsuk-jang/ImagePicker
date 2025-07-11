package com.jms.imagePicker.ui.preview

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.jms.imagePicker.model.MediaContent
import com.jms.imagePicker.ui.picker.ImagePickerState
import com.jms.imagePicker.ui.ImagePickerViewModel
import com.jms.imagePicker.ui.scope.PreviewScope
import com.jms.imagePicker.ui.scope.PreviewScopeImpl


@Composable
internal fun PreviewScaffold(
    modifier: Modifier = Modifier,
    viewModel: ImagePickerViewModel,
    state: ImagePickerState,
    onBack: () -> Unit = {},
    initializeFirstVisibleItemIndex: Int = 0,
    content: @Composable PreviewScope.(MediaContent) -> Unit = {}
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

        PreviewContent(
            modifier = modifier,
            mediaContents = mediaContents,
            initializeFirstVisibleItemIndex = initializeFirstVisibleItemIndex,
        ) {
            val previewScopeImpl = remember(viewModel, state) {
                PreviewScopeImpl(
                    boxScope = this,
                    viewModel = viewModel,
                    state = state
                )
            }

            content(previewScopeImpl, it)
        }
    }
}


@Composable
private fun PreviewContent(
    modifier: Modifier = Modifier,
    mediaContents: LazyPagingItems<MediaContent>,
    initializeFirstVisibleItemIndex: Int = 0,
    content: @Composable BoxScope.(MediaContent) -> Unit = {}
) {
    val context = LocalContext.current

    val listState = rememberPagerState(
        initialPage = initializeFirstVisibleItemIndex
    ) { mediaContents.itemCount }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black)
    ) {
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
}