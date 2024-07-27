package com.jms.galleryselector.ui

import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.jms.galleryselector.manager.API21MediaContentManager
import com.jms.galleryselector.manager.API29MediaContentManager
import com.jms.galleryselector.manager.FileManager
import com.jms.galleryselector.model.Album
import com.jms.galleryselector.model.Gallery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 *
 * @param album: selected album, when album is null, load total media content
 */
@Composable
fun GalleryScreen(
    state: GalleryState = rememberGalleryState(),
    album: Album = Album(id = null),
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

    val selectedAlbum by viewModel.selectedAlbum.collectAsState()

    if (album.id != selectedAlbum.id) {
        viewModel.setSelectedAlbum(album = album)
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

    val contents = viewModel.contents.collectAsLazyPagingItems(context = Dispatchers.Default)
    val cameraLaunch =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) {
            Log.e("jms8732", "$it", )
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
        onClick = {
            viewModel.select(image = it, max = state.max)
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
    onClick: (Gallery.Image) -> Unit,
    onPhoto: () -> Unit,
    content: @Composable BoxScope.(Gallery.Image) -> Unit
) {
    LazyVerticalGrid(
        modifier = modifier,
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

        items(images.itemCount, key = images.itemKey { it.id }) {
            images[it]?.let {
                Box(
                    modifier = Modifier
                        .clickable {
                            onClick(it)
                        }
                        .aspectRatio(1f)
                ) {
                    ImageCell(image = it)
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
    val selectedImagesState: State<List<Gallery.Image>> = _selectedImages

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

    val selectedAlbum: MutableState<Album> = mutableStateOf(Album(id = null))
}