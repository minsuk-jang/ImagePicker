package com.jms.galleryselector.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.jms.galleryselector.Constants
import com.jms.galleryselector.component.ImageCell
import com.jms.galleryselector.data.GalleryPagingStream
import com.jms.galleryselector.data.LocalGalleryDataSource
import com.jms.galleryselector.model.Gallery
import kotlinx.coroutines.Dispatchers

@Composable
fun GalleryScreen(
    state: GalleryState = rememberGalleryState(),
    selectFrame: @Composable (Gallery.Image) -> Unit
) {
    val context = LocalContext.current
    val viewModel: GalleryScreenViewModel = viewModel {
        GalleryScreenViewModel(
            localGalleryDataSource = LocalGalleryDataSource(
                context = context,
                galleryStream = GalleryPagingStream()
            )
        )
    }

    val images = viewModel.images.collectAsLazyPagingItems(context = Dispatchers.Default)

    GalleryScreen(
        images = images,
        selectFrame = selectFrame,
        onClick = {
            state.update(image = it)
            viewModel.select(image = it)
        }
    )
}


@Composable
private fun GalleryScreen(
    modifier: Modifier = Modifier,
    selectFrame: @Composable (Gallery.Image) -> Unit,
    images: LazyPagingItems<Gallery.Image>,
    onClick: (Gallery.Image) -> Unit
) {
    when {
        images.itemCount > 0 -> {
            LazyVerticalGrid(
                modifier = modifier,
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(3.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                items(images.itemCount, key = { it }) {
                    images[it]?.let {
                        Box(
                            modifier = Modifier
                                .clickable {
                                    onClick(it)
                                }
                                .aspectRatio(1f)
                        ) {
                            ImageCell(image = it)

                            if (it.selectedOrder != Constants.NO_ORDER)
                                selectFrame(it)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun rememberGalleryState(
    initialList: List<Gallery.Image> = emptyList()
): GalleryState {
    return remember {
        GalleryState(
            initialList = initialList
        )
    }
}

@Stable
class GalleryState(
    private val initialList: List<Gallery.Image> = emptyList(),
) {
    private val _selectedImages: MutableState<List<Gallery.Image>> =
        mutableStateOf(initialList.toMutableList())
    val selectedImagesState: State<List<Gallery.Image>> = _selectedImages

    internal fun update(image: Gallery.Image) {
        _selectedImages.value = _selectedImages.value.toMutableList().apply {
            val index = indexOfFirst { it.id == image.id }

            if (index == -1) {
                add(image)
            } else
                removeAt(index)
        }
    }
}