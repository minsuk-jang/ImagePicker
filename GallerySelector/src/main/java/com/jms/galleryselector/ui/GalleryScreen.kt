package com.jms.galleryselector.ui

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
import com.jms.galleryselector.manager.MediaContentManager
import com.jms.galleryselector.model.Gallery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun GalleryScreen(
    state: GalleryState = rememberGalleryState(),
    content: @Composable BoxScope.(Gallery.Image) -> Unit
) {
    val context = LocalContext.current
    val viewModel: GalleryScreenViewModel = viewModel {
        GalleryScreenViewModel(
            contentManager = MediaContentManager(),
            localGalleryDataSource = LocalGalleryDataSource(
                context = context,
                galleryStream = GalleryPagingStream()
            )
        )
    }

    LaunchedEffect(viewModel) {
        launch {
            viewModel.selectedImages.collectLatest {
                state.update(list = it)
            }
        }
    }

    val images = viewModel.getGalleryContents(page = 1)
        .collectAsLazyPagingItems(context = Dispatchers.Default)

    val cameraLaunch =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) {
            if (it) {
                viewModel.saveImageFile(context = context, max = state.max, autoSelectAfterCapture = state.autoSelectAfterCapture)
                images.refresh()
            }
        }


    GalleryScreen(
        images = images,
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
    when {
        images.itemCount > 0 -> {
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
    internal fun update(list: List<Gallery.Image>) {
        _selectedImages.value = list
    }
}