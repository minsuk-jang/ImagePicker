package com.jms.imagePicker.ui.picker

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.jms.imagePicker.Constants
import com.jms.imagePicker.R
import com.jms.imagePicker.component.ImageCell
import com.jms.imagePicker.extensions.photoGridDragHandler
import com.jms.imagePicker.model.Album
import com.jms.imagePicker.model.MediaContent
import com.jms.imagePicker.ui.ImagePickerViewModel
import com.jms.imagePicker.ui.scope.ImagePickerContentScope
import com.jms.imagePicker.ui.scope.picker.ImagePickerAlbumScope
import com.jms.imagePicker.ui.scope.picker.ImagePickerPreviewTopBarScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive

@Composable
internal fun ImagePickerScaffold(
    state: ImagePickerNavHostState = rememberImagePickerNavHostState(),
    viewModel: ImagePickerViewModel,
    onNavigateToPreview: (Int) -> Unit = {},
    albumTopBar: @Composable ImagePickerAlbumScope.() -> Unit = {},
    previewTopBar: @Composable ImagePickerPreviewTopBarScope.() -> Unit = {},
    content: @Composable ImagePickerContentScope.() -> Unit
) {
    val context = LocalContext.current
    val mediaContents = viewModel.mediaContents.collectAsLazyPagingItems()
    val selectedImages by viewModel.selectedMediaContents.collectAsState()
    val selectedUris by viewModel.selectedUris.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val selectedAlbum by viewModel.selectedAlbum.collectAsState()

    val cameraLaunch =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) {
            if (it) {
                viewModel.saveImageFile(
                    max = state.max,
                    autoSelectAfterCapture = state.autoSelectAfterCapture
                )
            }
        }

    val previewScopeImpl = remember(viewModel) {
        object : ImagePickerPreviewTopBarScope {
            override val selectedMediaContents: List<MediaContent>
                get() = selectedImages

            override fun onClick(mediaContent: MediaContent) {
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

            override fun onSelect(album: Album) {
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
            selectedUris = selectedUris,
            onDragStart = {
                viewModel.select(uri = it, max = state.max)
            },
            onDrag = { start, end, items ->
                viewModel.select(
                    start = start,
                    end = end,
                    mediaContents = items,
                    max = state.max
                )
            },
            onDragEnd = {
                viewModel.synchronize()
            },
            onPhoto = {
                cameraLaunch.launch(
                    FileProvider.getUriForFile(
                        context.applicationContext, "com.jms.imagePicker.fileprovider",
                        viewModel.createImageFile()
                    )
                )
            },
            onClick = {
                viewModel.select(uri = it.uri, max = state.max)
            },
            content = {
                val contentScopeImpl = remember(it) {
                    object : ImagePickerContentScope {
                        override val mediaContent: MediaContent
                            get() = it

                        override fun onNavigateToPreview(mediaContent: MediaContent) {
                            val index =
                                mediaContents.itemSnapshotList.indexOfFirst { it?.uri == mediaContent.uri }
                                    .coerceAtLeast(0)
                            onNavigateToPreview(index)
                        }
                    }
                }

                content(contentScopeImpl)
            }
        )
    }
}


@Composable
internal fun ImagePickerContent(
    modifier: Modifier = Modifier,
    mediaContents: LazyPagingItems<MediaContent>,
    selectedUris: List<Uri> = emptyList(),
    onPhoto: () -> Unit,
    onDragStart: (Uri) -> Unit,
    onDrag: (start: Int?, end: Int?, List<MediaContent>) -> Unit,
    onDragEnd: () -> Unit = {},
    onClick: (MediaContent) -> Unit = {},
    content: @Composable (MediaContent) -> Unit
) {
    val gridState = rememberLazyGridState()

    val autoScrollThreshold = with(LocalDensity.current) { 15.dp.toPx() }
    val autoScrollSpeed = remember { mutableFloatStateOf(0f) }
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val width = remember { screenWidthDp / 3 }
    val iconOfCamera = rememberVectorPainter(ImageVector.vectorResource(R.drawable.photo_camera))

    LaunchedEffect(Unit) {
        snapshotFlow { autoScrollSpeed.floatValue }
            .collectLatest { speed ->
                if (speed == 0f) return@collectLatest

                while (isActive) {
                    gridState.scrollBy(speed)
                    delay(5)
                }
            }
    }

    //when loading..
    if (mediaContents.loadState.refresh is LoadState.Loading) return

    LazyVerticalGrid(
        modifier = modifier
            .fillMaxSize()
            .photoGridDragHandler(
                lazyGridState = gridState,
                selectedUris = selectedUris,
                haptics = LocalHapticFeedback.current,
                onDragStart = onDragStart,
                autoScrollSpeed = autoScrollSpeed,
                autoScrollThreshold = autoScrollThreshold,
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
        item {
            Box(
                modifier = Modifier
                    .background(color = Color.LightGray)
                    .clickable { onPhoto() }
                    .aspectRatio(1f)
            ) {
                Icon(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(40.dp),
                    painter = iconOfCamera,
                    contentDescription = null,
                )
            }
        }

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

                    content(mediaContent)
                }
            }
        }
    }
}

@Composable
fun rememberImagePickerNavHostState(
    max: Int = Constants.MAX_SIZE,
    autoSelectAfterCapture: Boolean = false,
): ImagePickerNavHostState {
    return remember {
        ImagePickerNavHostState(
            max = max,
            autoSelectAfterCapture = autoSelectAfterCapture,
        )
    }
}

@Stable
class ImagePickerNavHostState(
    val max: Int = Constants.MAX_SIZE,
    val autoSelectAfterCapture: Boolean = false
) {
    private var _selectedMediaContents: MutableState<List<MediaContent>> =
        mutableStateOf(emptyList())

    //Selected Media Content list
    val selectedMediaContents: List<MediaContent> get() = _selectedMediaContents.value

    internal fun updateMediaContents(mediaContents: List<MediaContent>) {
        _selectedMediaContents.value = mediaContents
    }
}
