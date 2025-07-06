package com.jms.imagePicker.ui

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.jms.imagePicker.Constants
import com.jms.imagePicker.R
import com.jms.imagePicker.component.ImageCell
import com.jms.imagePicker.data.LocalGalleryDataSource
import com.jms.imagePicker.extensions.photoGridDragHandler
import com.jms.imagePicker.manager.API21MediaContentManager
import com.jms.imagePicker.manager.API29MediaContentManager
import com.jms.imagePicker.manager.FileManager
import com.jms.imagePicker.model.Album
import com.jms.imagePicker.model.MediaContent
import com.jms.imagePicker.ui.preview.PreviewScreen
import com.jms.imagePicker.ui.scope.ImagePickerAlbumScope
import com.jms.imagePicker.ui.scope.PreviewTopBarScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
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
    val context = LocalContext.current
    val navController = rememberNavController()
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) {
        NavHost(
            modifier = Modifier.padding(it),
            navController = navController,
            startDestination = "route_image_list",
            route = "graph_image_picker",
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            composable(
                route = "route_image_list"
            ) {
                val parentEntry = remember(it) {
                    navController.getBackStackEntry("graph_image_picker")
                }

                val viewModel: ImagePickerViewModel = viewModel(parentEntry) {
                    ImagePickerViewModel(
                        fileManager = FileManager(context = context.applicationContext),
                        localGalleryDataSource = LocalGalleryDataSource(
                            contentManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                API29MediaContentManager(context = context.applicationContext)
                            } else
                                API21MediaContentManager(context = context.applicationContext)
                        )
                    )
                }

                ImagePickerScaffold(
                    viewModel = viewModel,
                    state = state,
                    albumTopBar = albumTopBar,
                    content = content,
                    previewTopBar = previewTopBar,
                    onNavigateToPreview = {
                        navController.navigate("route_preview?$it") {
                            launchSingleTop = true

                            popUpTo("route_image_list") {
                                saveState = true
                            }
                        }
                    }
                )
            }

            composable(
                route = "route_preview?{index}",
                arguments = listOf(
                    navArgument("index") {
                        type = NavType.IntType
                    }
                )
            ) {
                val parentEntry = remember(it) {
                    navController.getBackStackEntry("graph_image_picker")
                }

                val viewModel: ImagePickerViewModel = viewModel(parentEntry)
                val initializeFirstVisibleItemIndex = it.arguments?.getInt("index") ?: 0

                PreviewScreen(
                    viewModel = viewModel,
                    initializeFirstVisibleItemIndex = initializeFirstVisibleItemIndex,
                    onBack = {
                        navController.popBackStack(
                            route = "route_image_list",
                            inclusive = false
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ImagePickerScaffold(
    modifier: Modifier = Modifier,
    state: ImagePickerState = rememberImagePickerState(),
    viewModel: ImagePickerViewModel,
    onNavigateToPreview: (Int) -> Unit = {},
    albumTopBar: @Composable ImagePickerAlbumScope.() -> Unit = {},
    previewTopBar: @Composable PreviewTopBarScope.() -> Unit = {},
    content: @Composable BoxScope.(MediaContent) -> Unit
) {
    val context = LocalContext.current
    val mediaContents = viewModel.mediaContents.collectAsLazyPagingItems()
    val selectedImages by viewModel.selectedImages.collectAsState()
    val selectedUris by viewModel.selectedUris.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val selectedAlbum by viewModel.selectedAlbum.collectAsState()

    LaunchedEffect(viewModel) {
        launch {
            viewModel.selectedImages.collectLatest {
                state.updateImages(list = it)
            }
        }
    }

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
        object : PreviewTopBarScope {
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
        modifier = modifier
    ) {
        albumScopeImpl.albumTopBar()
        previewScopeImpl.previewTopBar()

        ImagePickerContent(
            mediaContents = mediaContents,
            selectedUris = selectedUris,
            onClick = {
                viewModel.select(uri = it, max = state.max)
            },
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
    val savedPos = rememberSaveable { mutableStateOf<Pair<Int, Int>?>(null) }
    val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = savedPos.value?.first ?: 0,
        initialFirstVisibleItemScrollOffset = savedPos.value?.second ?: 0
    )

    val autoScrollThreshold = with(LocalDensity.current) { 15.dp.toPx() }
    val autoScrollSpeed = remember { mutableFloatStateOf(0f) }
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val width = remember { screenWidthDp / 3 }
    val iconOfExpandContent = rememberVectorPainter(
        ImageVector.vectorResource(R.drawable.expand_content)
    )
    val iconOfCamera = rememberVectorPainter(ImageVector.vectorResource(R.drawable.photo_camera))

    LaunchedEffect(mediaContents.loadState.refresh) {
        val (idx, offset) = savedPos.value ?: return@LaunchedEffect
        if (mediaContents.loadState.refresh is LoadState.NotLoading && mediaContents.itemCount >= idx) {
            gridState.scrollToItem(index = idx, scrollOffset = offset)
            savedPos.value = null
        }
    }

    LaunchedEffect(gridState) {
        snapshotFlow { gridState.isScrollInProgress}
            .filter { !it }
            .distinctUntilChanged()
            .collectLatest {
                savedPos.value = gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset
            }
    }

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