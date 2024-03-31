package com.jms.galleryselector.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.jms.galleryselector.data.LocalGalleryDataSource
import com.jms.galleryselector.model.Gallery
import com.jms.galleryselector.model.GalleryContentSortBy
import com.jms.galleryselector.model.toImage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

internal class GalleryScreenViewModel constructor(
    val localGalleryDataSource: LocalGalleryDataSource
) : ViewModel() {
    //selected gallery ids
    private val _selectedImages = MutableStateFlow<List<Gallery.Image>>(mutableListOf())
    val selectedImages: StateFlow<List<Gallery.Image>> = _selectedImages.asStateFlow()

    fun getGalleryContents(
        page: Int = 1,
        sortBy: GalleryContentSortBy
    ): Flow<PagingData<Gallery.Image>> {
        return localGalleryDataSource.getLocalGalleryImages(page = page, sortBy = sortBy)
            .map {
                it.map { it.toImage() }
            }
            .cachedIn(viewModelScope)
            .combine(_selectedImages) { data, images ->
                update(pagingData = data, selectedImages = images)
            }
    }

    fun select(image: Gallery.Image, max: Int) {
        _selectedImages.update {
            it.toMutableList().apply {
                val index = indexOfFirst { it.id == image.id }

                if (index == -1) {
                    //limit max size
                    if (_selectedImages.value.size < max)
                        add(
                            image.copy(
                                selectedOrder = size,
                                selected = true
                            )
                        )
                } else {
                    removeAt(index)
                    invalidSelectedOrdering(list = this, start = index)
                }
            }
        }
    }

    private fun invalidSelectedOrdering(list: MutableList<Gallery.Image>, start: Int) {
        for (i in start until list.size) {
            val item = list[i]
            list[i] = item.copy(
                selectedOrder = item.selectedOrder - 1
            )
        }
    }

    private fun update(
        pagingData: PagingData<Gallery.Image>,
        selectedImages: List<Gallery.Image>
    ): PagingData<Gallery.Image> {
        return pagingData.map { image ->
            image.copy(
                selectedOrder = selectedImages.indexOfFirst { it.id == image.id },
                selected = selectedImages.firstOrNull { it.id == image.id }?.selected ?: false
            )
        }
    }
}