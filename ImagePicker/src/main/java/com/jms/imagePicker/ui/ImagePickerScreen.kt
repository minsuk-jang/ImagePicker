package com.jms.imagePicker.ui

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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.jms.galleryselector.R
import com.jms.imagePicker.Constants
import com.jms.imagePicker.component.ImageCell
import com.jms.imagePicker.data.GalleryPagingStream
import com.jms.imagePicker.data.LocalGalleryDataSource
import com.jms.imagePicker.extensions.photoGridDragHandler
import com.jms.imagePicker.manager.API21MediaContentManager
import com.jms.imagePicker.manager.API29MediaContentManager
import com.jms.imagePicker.manager.FileManager
import com.jms.imagePicker.model.Album
import com.jms.imagePicker.model.Gallery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 *
 * @param album: selected album, when album is null, load total media content
 */

@Composable
fun ImagePickerScreen(
    state: ImagePickerState = rememberImagePickerState(),
    album: Album? = null,
    onClick: (Gallery.Image) -> Unit = {},
    content: @Composable BoxScope.(Gallery.Image) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val viewModel: ImagePickerScreenViewModel = viewModel {
        ImagePickerScreenViewModel(
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
                state.updateAlbum(album = it)
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


    ImagePickerScreen(
        images = contents,
        content = content,
        selectedUris = selectedUris,
        onClick = {
            viewModel.select(uri = it.uri, max = state.max)
            onClick(it.copy(selected = !it.selected))
        },
        onDragStart = {
            viewModel.select(uri = it, max = state.max)
        },
        onDrag = { start, end, items ->
            viewModel.select(
                start = start,
                end = end,
                images = items,
                max = state.max
            )
        },
        onDragEnd = {
            viewModel.synchronize()
        },
        onPhoto = {
            scope.launch(Dispatchers.IO) {
                val file = viewModel.createImageFile()
                cameraLaunch.launch(
                    FileProvider.getUriForFile(
                        context, "com.jms.galleryselector.fileprovider",
                        file
                    )
                )
            }

        }
    )
}


@Composable
private fun ImagePickerScreen(
    modifier: Modifier = Modifier,
    images: LazyPagingItems<Gallery.Image>,
    selectedUris: List<Uri>,
    onClick: (Gallery.Image) -> Unit,
    onPhoto: () -> Unit,
    onDragStart: (Uri) -> Unit,
    onDrag: (start: Int?, end: Int?, List<Gallery.Image>) -> Unit,
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
                onDrag = { start, end ->
                    onDrag(
                        start,
                        end,
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

                    if (it.selected)
                        content(it)
                }
            }
        }
    }
}

@Composable
fun rememberImagePickerState(
    max: Int = Constants.MAX_SIZE,
    autoSelectAfterCapture: Boolean = false
): ImagePickerState {
    return remember {
        ImagePickerState(
            max = max,
            autoSelectAfterCapture = autoSelectAfterCapture
        )
    }
}

@Stable
class ImagePickerState(
    val max: Int = Constants.MAX_SIZE,
    val autoSelectAfterCapture: Boolean = false
) {
    private val _pickedImages: MutableStateFlow<List<Gallery.Image>> = MutableStateFlow(emptyList())
    val pickedImages: StateFlow<List<Gallery.Image>> = _pickedImages.asStateFlow()

    //update images
    internal fun updateImages(list: List<Gallery.Image>) {
        _pickedImages.update { list }
    }

    private val _albums: MutableStateFlow<List<Album>> = MutableStateFlow(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    //update albums
    internal fun updateAlbums(list: List<Album>) {
        _albums.value = list
    }

    private val _pickedAlbum: MutableStateFlow<Album?> = MutableStateFlow(null)
    val pickedAlbum: StateFlow<Album?> = _pickedAlbum.asStateFlow()

    internal fun updateAlbum(album: Album) {
        _pickedAlbum.update { album }
    }
}