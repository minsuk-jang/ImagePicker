package com.jms.imagePicker.ui

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
import com.jms.imagePicker.model.Gallery
import com.jms.imagePicker.ui.preview.PreviewScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 *
 * @param album: selected album, when album is null, load total media content
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePickerScreen(
    state: ImagePickerState = rememberImagePickerState(),
    album: Album? = null,
    onAlbumListLoaded: (List<Album>) -> Unit = {},
    onAlbumSelected: (Album) -> Unit = {},
    topBar: @Composable () -> Unit = {},
    previewTopBar: @Composable () -> Unit = {},
    content: @Composable BoxScope.(Gallery.Image) -> Unit
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
            route = "graph_image_picker"
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

                ImagePickerScreen(
                    viewModel = viewModel,
                    onAlbumSelected = onAlbumSelected,
                    onAlbumListLoaded = onAlbumListLoaded,
                    state = state,
                    album = album,
                    topBar = topBar,
                    previewTopBar = previewTopBar,
                    content = content,
                    onNavigateToPreview = {
                        navController.navigate("route_preview?$it") {
                            launchSingleTop = true
                            restoreState = true

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
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
private fun ImagePickerScreen(
    modifier: Modifier = Modifier,
    state: ImagePickerState = rememberImagePickerState(),
    viewModel: ImagePickerViewModel,
    album: Album? = null,
    onAlbumListLoaded: (List<Album>) -> Unit = {},
    onAlbumSelected: (Album) -> Unit = {},
    onNavigateToPreview: (Int) -> Unit = {},
    topBar: @Composable () -> Unit = {},
    previewTopBar: @Composable () -> Unit = {},
    content: @Composable BoxScope.(Gallery.Image) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val images = viewModel.images.collectAsLazyPagingItems()
    val selectedUris by viewModel.selectedUris.collectAsState()

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
                onAlbumListLoaded(it)
            }
        }

        launch {
            viewModel.selectedAlbum.collectLatest {
                onAlbumSelected(it)
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

    SubcomposeLayout(
        modifier = modifier
    ) { constraints ->
        val topBarMeasurable = subcompose("top_bar", topBar).firstOrNull()
        val previewTopBarMeasurable = subcompose("preview_top_bar", previewTopBar).firstOrNull()

        val pickerContentMeasurable = subcompose("picker_content") {
            ImagePickerContent(
                images = images,
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
                                context, "com.jms.imagePicker.fileprovider",
                                file
                            )
                        )
                    }
                },
                onNavigateToPreview = { image ->
                    val index =
                        images.itemSnapshotList.indexOfFirst { it?.uri == image.uri }
                            .coerceAtLeast(0)
                    onNavigateToPreview(index)
                },
                content = content
            )
        }.first()

        val pickerContentPlaceable = pickerContentMeasurable.measure(constraints)

        val topBarPlaceable = topBarMeasurable?.measure(constraints)
        var topBarHeight = topBarPlaceable?.height ?: 0

        var previewTopBarPlaceable = previewTopBarMeasurable?.measure(constraints)
        var previewTopBarHeight = previewTopBarPlaceable?.height ?: 0


        layout(width = constraints.maxWidth, height = constraints.maxHeight) {
            topBarPlaceable?.place(0, 0)
            previewTopBarPlaceable?.place(0, topBarHeight)
            pickerContentPlaceable.place(0, topBarHeight + previewTopBarHeight)
        }
    }
}


@Composable
private fun ImagePickerContent(
    modifier: Modifier = Modifier,
    images: LazyPagingItems<Gallery.Image>,
    selectedUris: List<Uri>,
    onClick: (Uri) -> Unit,
    onPhoto: () -> Unit,
    onDragStart: (Uri) -> Unit,
    onDrag: (start: Int?, end: Int?, List<Gallery.Image>) -> Unit,
    onDragEnd: () -> Unit = {},
    onNavigateToPreview: (Gallery.Image) -> Unit = { },
    content: @Composable BoxScope.(Gallery.Image) -> Unit
) {
    val gridState = rememberSaveable(saver = LazyGridState.Saver) { LazyGridState() }
    val autoScrollSpeed = remember { mutableFloatStateOf(0f) }

    LaunchedEffect(key1 = autoScrollSpeed.floatValue) {
        if (autoScrollSpeed.floatValue != 0f) {
            while (isActive) {
                gridState.scrollBy(autoScrollSpeed.floatValue)
                delay(5)
            }
        }
    }

    LazyVerticalGrid(
        modifier = modifier
            .fillMaxSize()
            .photoGridDragHandler(
                lazyGridState = gridState,
                selectedImages = selectedUris,
                haptics = LocalHapticFeedback.current,
                onDragStart = onDragStart,
                autoScrollSpeed = autoScrollSpeed,
                autoScrollThreshold = with(LocalDensity.current) { 15.dp.toPx() },
                onDrag = { start, end ->
                    onDrag(
                        start,
                        end,
                        images.itemSnapshotList.items
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
                    painter = painterResource(id = R.drawable.photo_camera),
                    contentDescription = null,
                )
            }
        }

        items(
            count = images.itemCount,
            key = images.itemKey { it.uri }
        ) {
            images[it]?.let {
                Box(
                    modifier = Modifier
                        .clickable { onClick(it.uri) }
                        .aspectRatio(1f)
                ) {
                    ImageCell(
                        modifier = Modifier.matchParentSize(),
                        image = it,
                    )

                    if (it.selected)
                        content(it)

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
                                        shape = RoundedCornerShape(2.dp)
                                    )
                                    .clickable {
                                        onNavigateToPreview(it)
                                    },
                                painter = painterResource(id = R.drawable.expand_content),
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
    private var _pickedImages: MutableState<List<Gallery.Image>> = mutableStateOf(emptyList())
    val images: State<List<Gallery.Image>> = _pickedImages

    //update images
    internal fun updateImages(list: List<Gallery.Image>) {
        _pickedImages.value = list
    }
}