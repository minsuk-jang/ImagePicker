package com.jms.galleryselector.ui

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.jms.galleryselector.Constants
import com.jms.galleryselector.R
import com.jms.galleryselector.component.ImageCell
import com.jms.galleryselector.data.GalleryPagingStream
import com.jms.galleryselector.data.LocalGalleryDataSource
import com.jms.galleryselector.extensions.photoGridDragHandler
import com.jms.galleryselector.manager.API21MediaContentManager
import com.jms.galleryselector.manager.API29MediaContentManager
import com.jms.galleryselector.manager.FileManager
import com.jms.galleryselector.model.Album
import com.jms.galleryselector.model.Gallery
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 *
 * @param album: selected album, when album is null, load total media content
 */

@Composable
fun GalleryScreen(
    state: GalleryState = rememberGalleryState(),
    album: Album? = null,
    content: @Composable BoxScope.(Gallery.Image) -> Unit
) {
    val context = LocalContext.current
    val viewModel: GalleryScreenViewModel = viewModel {
        GalleryScreenViewModel(
            fileManager = FileManager(context = context),
            localGalleryDataSource = LocalGalleryDataSource(
                contentManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    API29MediaContentManager(context = context)
                } else
                    API21MediaContentManager(context = context),
                galleryStream = GalleryPagingStream()
            )
        )
    }

    if (album != null) {
        LaunchedEffect(key1 = album) {
            viewModel.setSelectedAlbum(album = album)
        }
    }

    LaunchedEffect(viewModel) {
        launch {
            viewModel.selectedImages.collectLatest {
                state.updateImages(list = it)
            }
        }
        launch {
            viewModel.albums.collectLatest {
                state.updateAlbums(list = it)
            }
        }

        launch {
            viewModel.selectedAlbum.collectLatest {
                state.selectedAlbum.value = it
            }
        }
    }

    val contents = viewModel.contents.collectAsLazyPagingItems()
    val selectedUris by viewModel.selectedUris.collectAsState()
    val cameraLaunch =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) {
            if (it) {
                viewModel.saveImageFile(
                    context = context,
                    max = state.max,
                    autoSelectAfterCapture = state.autoSelectAfterCapture
                )

                viewModel.refresh()
                contents.refresh()
            }
        }


    GalleryScreen(
        images = contents,
        content = content,
        selectedUris = selectedUris,
        onClick = {
            viewModel.select(uri = it.uri, max = state.max)
        },
        onDragStart = {
            viewModel.select(uri = it, max = state.max)
        },
        onDrag = { start, middle, end, pivot, curRow, prevRow, items ->
            viewModel.select(
                start = start,
                middle = middle,
                end = end,
                pivot = pivot,
                curRow = curRow,
                prevRow = prevRow,
                images = items,
                max = state.max
            )
        },
        onDragEnd = {
            viewModel.synchronize()
        },
        onPhoto = {
            val file = viewModel.createImageFile()

            cameraLaunch.launch(
                FileProvider.getUriForFile(
                    context, "com.jms.galleryselector.fileprovider",
                    file
                )
            )
        }
    )
}


@Composable
private fun GalleryScreen(
    modifier: Modifier = Modifier,
    images: LazyPagingItems<Gallery.Image>,
    selectedUris: List<Uri>,
    onClick: (Gallery.Image) -> Unit,
    onPhoto: () -> Unit,
    onDragStart: (Uri) -> Unit,
    onDrag: (start: Int?, middle: Int?, end: Int?, pivot: Int?, curRow: Int?, prevRow: Int?, List<Gallery.Image>) -> Unit,
    onDragEnd: () -> Unit = {},
    content: @Composable BoxScope.(Gallery.Image) -> Unit
) {
    val state = rememberLazyGridState()
    val autoScrollSpeed = remember { mutableFloatStateOf(0f) }

    LaunchedEffect(key1 = autoScrollSpeed.floatValue) {
        if (autoScrollSpeed.floatValue != 0f) {
            while (isActive) {
                state.scrollBy(autoScrollSpeed.floatValue)
                delay(10)
            }
        }
    }

    LazyVerticalGrid(
        modifier = modifier
            .photoGridDragHandler(
                lazyGridState = state,
                selectedImages = selectedUris,
                haptics = LocalHapticFeedback.current,
                onDragStart = onDragStart,
                autoScrollSpeed = autoScrollSpeed,
                autoScrollThreshold = with(LocalDensity.current) { 30.dp.toPx() },
                onDrag = { start, middle, end, pivot, curRow, prevRow ->
                    onDrag(
                        start,
                        middle,
                        end,
                        pivot,
                        curRow,
                        prevRow,
                        images.itemSnapshotList.items
                    )
                },
                onDragEnd = onDragEnd
            ),
        state = state,
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        item {
            Box(
                modifier = Modifier
                    .background(color = Color.LightGray)
                    .clickable {
                        onPhoto()
                    }
                    .aspectRatio(1f)
            ) {
                Icon(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(39.dp),
                    painter = painterResource(id = R.drawable.photo_camera),
                    contentDescription = null,
                )
            }
        }

        items(
            count = images.itemCount,
            key = images.itemKey { it.uri },
        ) {
            images[it]?.let {
                Box(
                    modifier = Modifier
                        .clickable {
                            onClick(it)
                        }
                        .aspectRatio(1f)
                ) {
                    ImageCell(
                        modifier = Modifier.matchParentSize(),
                        image = it
                    )
                    content(it)
                }
            }
        }
    }
}

@Composable
fun rememberGalleryState(
    max: Int = Constants.MAX_SIZE,
    autoSelectAfterCapture: Boolean = false
): GalleryState {
    return remember {
        GalleryState(
            max = max,
            autoSelectAfterCapture = autoSelectAfterCapture
        )
    }
}

@Stable
class GalleryState(
    val max: Int = Constants.MAX_SIZE,
    val autoSelectAfterCapture: Boolean = false
) {
    private val _selectedImages: MutableState<List<Gallery.Image>> = mutableStateOf(emptyList())
    val selectedImages: State<List<Gallery.Image>> = _selectedImages

    //update images
    internal fun updateImages(list: List<Gallery.Image>) {
        _selectedImages.value = list
    }

    private val _albums: MutableState<List<Album>> = mutableStateOf(emptyList())
    val albums: State<List<Album>> = _albums

    //update albums
    internal fun updateAlbums(list: List<Album>) {
        _albums.value = list
    }

    val selectedAlbum: MutableState<Album?> = mutableStateOf(null)
}