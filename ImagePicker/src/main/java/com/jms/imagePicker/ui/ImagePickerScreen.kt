package com.jms.imagePicker.ui

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
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
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.jms.imagePicker.Constants
import com.jms.imagePicker.R
import com.jms.imagePicker.component.ImageCell
import com.jms.imagePicker.extensions.photoGridDragHandler
import com.jms.imagePicker.manager.ImagePickerManager
import com.jms.imagePicker.model.Album
import com.jms.imagePicker.model.MediaContent
import com.jms.imagePicker.ui.scope.ImagePickerAlbumScope
import com.jms.imagePicker.ui.scope.PreviewTopBarScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePickerScreen(
    state: ImagePickerState = rememberImagePickerState(),
    albumTopBar: @Composable ImagePickerAlbumScope.() -> Unit = {},
    previewTopBar: @Composable PreviewTopBarScope.() -> Unit = {},
    content: @Composable BoxScope.(MediaContent) -> Unit
) {
    val context = LocalContext.current.applicationContext
    val activity = LocalContext.current as Activity
    val pickerManager = ImagePickerManager.getInstance(context = context)

    DisposableEffect(Unit) {
        pickerManager.attach()

        onDispose {
            //TODO check to change configuration
            if (activity.isChangingConfigurations)
                pickerManager.detach()
        }
    }

    ImagePickerScaffold(
        pickerManager = pickerManager,
        state = state,
        albumTopBar = albumTopBar,
        content = content,
        previewTopBar = previewTopBar,
        onNavigateToPreview = {

        }
    )
}

@Composable
private fun ImagePickerScaffold(
    modifier: Modifier = Modifier,
    state: ImagePickerState = rememberImagePickerState(),
    pickerManager: ImagePickerManager,
    onNavigateToPreview: (Int) -> Unit = {},
    albumTopBar: @Composable ImagePickerAlbumScope.() -> Unit = {},
    previewTopBar: @Composable PreviewTopBarScope.() -> Unit = {},
    content: @Composable BoxScope.(MediaContent) -> Unit
) {
    val mediaContents = pickerManager.mediaContents.collectAsLazyPagingItems()
    val selectedImages by pickerManager.selectedImages.collectAsState()
    val selectedUris by pickerManager.selectedUris.collectAsState()
    val albums by pickerManager.albums.collectAsState()
    val selectedAlbum by pickerManager.selectedAlbum.collectAsState()

    LaunchedEffect(pickerManager) {
        launch {
            pickerManager.selectedImages.collectLatest {
                state.updateImages(list = it)
            }
        }
    }

    val cameraLaunch =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) {
            //TODO navigate to camera
        }

    val previewScopeImpl = remember(pickerManager) {
        object : PreviewTopBarScope {
            override val selectedMediaContents: List<MediaContent>
                get() = selectedImages

            override fun onClick(mediaContent: MediaContent) {
                pickerManager.select(uri = mediaContent.uri, max = state.max)
            }
        }
    }

    val albumScopeImpl = remember(pickerManager) {
        object : ImagePickerAlbumScope {
            override val albums: List<Album>
                get() = albums

            override val selectedAlbum: Album?
                get() = selectedAlbum

            override fun onSelect(album: Album) {
                pickerManager.selectedAlbum(album = album)
            }
        }
    }

    Column(
        modifier = modifier
    ) {
        albumScopeImpl.albumTopBar()
        previewScopeImpl.previewTopBar()

        ImagePickerContent(
            mediaContents = mediaContents,
            selectedUris = selectedUris,
            onClick = {
                pickerManager.select(uri = it, max = state.max)
            },
            onDragStart = {
                pickerManager.select(uri = it, max = state.max)
            },
            onDrag = { start, end, items ->
                pickerManager.select(
                    start = start,
                    end = end,
                    mediaContents = items,
                    max = state.max
                )
            },
            onDragEnd = {
                pickerManager.synchronize()
            },
            onPhoto = {
                //TODO handle camera navigate
            },
            onNavigateToPreview = { image ->
                val index =
                    mediaContents.itemSnapshotList.indexOfFirst { it?.uri == image.uri }
                        .coerceAtLeast(0)
                onNavigateToPreview(index)
            },
            content = content
        )
    }
}


@Composable
private fun ImagePickerContent(
    modifier: Modifier = Modifier,
    mediaContents: LazyPagingItems<MediaContent>,
    selectedUris: List<Uri> = emptyList(),
    onClick: (Uri) -> Unit,
    onPhoto: () -> Unit,
    onDragStart: (Uri) -> Unit,
    onDrag: (start: Int?, end: Int?, List<MediaContent>) -> Unit,
    onDragEnd: () -> Unit = {},
    onNavigateToPreview: (MediaContent) -> Unit = { },
    content: @Composable BoxScope.(MediaContent) -> Unit
) {
    val gridState = rememberLazyGridState()

    val autoScrollThreshold = with(LocalDensity.current) { 15.dp.toPx() }
    val autoScrollSpeed = remember { mutableFloatStateOf(0f) }
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val width = remember { screenWidthDp / 3 }
    val iconOfExpandContent = rememberVectorPainter(
        ImageVector.vectorResource(R.drawable.expand_content)
    )
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
            mediaContents[it]?.let { content ->
                Box(
                    modifier = Modifier
                        .clickable {
                            onClick(content.uri)
                        }
                        .aspectRatio(1f)
                ) {
                    ImageCell(
                        modifier = Modifier.fillMaxSize(),
                        cellDp = width.dp,
                        mediaContent = content,
                    )

                    if (content.selected)
                        content(content)

                    Row(
                        modifier = Modifier.align(Alignment.BottomStart)
                    ) {
                        Spacer(modifier = Modifier.width(3.dp))
                        Column {
                            Icon(
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(5.dp)
                                    )
                                    .clickable {
                                        onNavigateToPreview(content)
                                    },
                                painter = iconOfExpandContent,
                                contentDescription = "expand_content",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun rememberImagePickerState(
    max: Int = Constants.MAX_SIZE,
    autoSelectAfterCapture: Boolean = false,
): ImagePickerState {
    return remember {
        ImagePickerState(
            max = max,
            autoSelectAfterCapture = autoSelectAfterCapture,
        )
    }
}

@Stable
class ImagePickerState(
    val max: Int = Constants.MAX_SIZE,
    val autoSelectAfterCapture: Boolean = false
) {
    private var _mediaContents: MutableState<List<MediaContent>> =
        mutableStateOf(emptyList())
    val mediaContents: State<List<MediaContent>> = _mediaContents

    //update images
    internal fun updateImages(list: List<MediaContent>) {
        _mediaContents.value = list
    }
}