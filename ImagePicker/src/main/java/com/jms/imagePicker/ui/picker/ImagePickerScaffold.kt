package com.jms.imagePicker.ui.picker

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.jms.imagePicker.component.ImageCell
import com.jms.imagePicker.extensions.photoGridDragHandler
import com.jms.imagePicker.model.Album
import com.jms.imagePicker.model.MediaContent
import com.jms.imagePicker.ui.ImagePickerViewModel
import com.jms.imagePicker.ui.scope.ImagePickerCellScope
import com.jms.imagePicker.ui.scope.picker.ImagePickerAlbumScope
import com.jms.imagePicker.ui.scope.picker.ImagePickerPreviewScope
import com.jms.imagePicker.ui.state.ImagePickerNavHostState
import com.jms.imagePicker.ui.state.rememberImagePickerNavHostState

@Composable
internal fun ImagePickerScaffold(
    state: ImagePickerNavHostState = rememberImagePickerNavHostState(),
    viewModel: ImagePickerViewModel,
    onNavigateToPreview: (Int) -> Unit = {},
    albumTopBar: @Composable ImagePickerAlbumScope.() -> Unit = {},
    previewTopBar: @Composable ImagePickerPreviewScope.() -> Unit = {},
    cellContent: @Composable ImagePickerCellScope.() -> Unit
) {
    val mediaContents = viewModel.mediaContents.collectAsLazyPagingItems()
    val selectedImages by viewModel.selectedMediaContents.collectAsState()
    val selectedUris by viewModel.selectedUris.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val selectedAlbum by viewModel.selectedAlbum.collectAsState()

    val currentSelectedUris = rememberUpdatedState(selectedUris)

    val previewScopeImpl = remember(viewModel) {
        object : ImagePickerPreviewScope {
            override val selectedMediaContents: List<MediaContent>
                get() = selectedImages

            override fun onDeselect(mediaContent: MediaContent) {
                viewModel.select(uri = mediaContent.uri, max = state.max)
            }
        }
    }

    val albumScopeImpl = remember(viewModel) {
        object : ImagePickerAlbumScope {
            override val albums: List<Album>
                get() = albums

            override val selectedAlbum: Album?
                get() = selectedAlbum

            override fun onClick(album: Album) {
                viewModel.selectedAlbum(album = album)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        albumScopeImpl.albumTopBar()
        previewScopeImpl.previewTopBar()

        ImagePickerContent(
            mediaContents = mediaContents,
            selectedUris = currentSelectedUris,
            onDragStart = {
                viewModel.startDrag(uri = it, max = state.max)
            },
            onDrag = { start, end, items ->
                if (start != null && end != null) {
                    viewModel.updateDragSelection(
                        start = start,
                        end = end,
                        mediaContents = items,
                        max = state.max
                    )
                }
            },
            onDragEnd = {
                viewModel.endDrag()
            },
            onClick = {
                viewModel.select(uri = it.uri, max = state.max)
            },
            cellContent = {
                val cellScopeImpl = remember(it) {
                    object : ImagePickerCellScope {
                        override val mediaContent: MediaContent
                            get() = it

                        override fun onNavigateToPreviewScreen(mediaContent: MediaContent) {
                            val index =
                                mediaContents.itemSnapshotList.indexOfFirst { it?.uri == mediaContent.uri }
                                    .coerceAtLeast(0)
                            onNavigateToPreview(index)
                        }
                    }
                }

                cellContent(cellScopeImpl)
            }
        )
    }
}


@Composable
internal fun ImagePickerContent(
    modifier: Modifier = Modifier,
    mediaContents: LazyPagingItems<MediaContent>,
    selectedUris: State<List<Uri>>,
    onDragStart: (Uri) -> Unit,
    onDrag: (start: Int?, end: Int?, List<MediaContent>) -> Unit,
    onDragEnd: () -> Unit = {},
    onClick: (MediaContent) -> Unit = {},
    cellContent: @Composable (MediaContent) -> Unit
) {
    val gridState = rememberLazyGridState()
    val autoScrollThreshold = with(LocalDensity.current) { 15.dp.toPx() }
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val width = remember { screenWidthDp / 3 }

    if (mediaContents.loadState.refresh is LoadState.Loading) return

    LazyVerticalGrid(
        modifier = modifier
            .fillMaxSize()
            .photoGridDragHandler(
                lazyGridState = gridState,
                selectedUris = selectedUris,
                haptics = LocalHapticFeedback.current,
                autoScrollThreshold = autoScrollThreshold,
                onDragStart = onDragStart,
                onDrag = { start, end ->
                    onDrag(
                        start,
                        end,
                        mediaContents.itemSnapshotList.items
                    )
                },
                onDragEnd = onDragEnd
            ),
        state = gridState,
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        items(
            count = mediaContents.itemCount,
            key = mediaContents.itemKey { it.uri }
        ) {
            mediaContents[it]?.let { mediaContent ->
                Box(
                    modifier = Modifier
                        .clickable {
                            onClick(mediaContent)
                        }
                        .aspectRatio(1f)
                ) {
                    ImageCell(
                        modifier = Modifier.fillMaxSize(),
                        cellDp = width.dp,
                        mediaContent = mediaContent,
                    )

                    cellContent(mediaContent)
                }
            }
        }
    }
}
