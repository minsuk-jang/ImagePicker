package com.jms.imagePicker.ui.preview

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.jms.imagePicker.ui.ImagePickerViewModel
import kotlin.math.max


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
            onBack = onBack,
            initializeFirstVisibleItemIndex = initializeFirstVisibleItemIndex,
            onSelect = {
                viewModel.select(uri = it.uri, max = 30)
            }
        )
    }
}


@Composable
private fun PreviewContent(
    modifier: Modifier = Modifier,
    mediaContents: LazyPagingItems<MediaContent>,
    initializeFirstVisibleItemIndex: Int = 0,
    onBack: () -> Unit = {},
    onSelect: (MediaContent) -> Unit = {}
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
                mediaContents[it]?.let {
                    AsyncImage(
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.Center),
                        model = ImageRequest.Builder(context)
                            .data(it.uri)
                            .build(),
                        contentDescription = "content",
                        filterQuality = FilterQuality.High
                    )
                }
            }

            mediaContents[listState.currentPage]?.let {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = when (it.selected) {
                                    true -> Color.White.copy(alpha = 0.9f)
                                    false -> Color.DarkGray.copy(alpha = 0.5f)
                                },
                                shape = CircleShape
                            )
                            .border(
                                border = BorderStroke(
                                    width = 3.dp,
                                    color = when (it.selected) {
                                        true -> Color.White.copy(alpha = 0.9f)
                                        false -> Color.LightGray
                                    }
                                ),
                                shape = CircleShape
                            )
                            .clickable {
                                onSelect(it)
                            }
                    ) {
                        if (it.selected) {
                            Text(
                                modifier = Modifier
                                    .align(Alignment.Center),
                                text = "${it.selectedOrder + 1}",
                                style = TextStyle(
                                    fontWeight = FontWeight.W500,
                                    color = Color.Black,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }

                    Spacer(Modifier.width(5.dp))
                }
            }
        }
    }
}