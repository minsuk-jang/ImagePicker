package com.jms.galleryselector.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.jms.galleryselector.Constants
import com.jms.galleryselector.data.LocalGalleryDataSource
import com.jms.galleryselector.model.Gallery
import com.jms.galleryselector.model.toImage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

internal class GalleryScreenViewModel constructor(
    localGalleryDataSource: LocalGalleryDataSource
) : ViewModel() {

    //selected gallery ids
    private val _selectedIds = MutableStateFlow<List<Long>>(mutableListOf())

    val images: Flow<PagingData<Gallery.Image>> =
        localGalleryDataSource.getLocalGalleryImages(page = 1)
            .map {
                it.map { it.toImage() }
            }
            .cachedIn(viewModelScope)
            .combine(_selectedIds) { data, ids ->
                update(pagingData = data, selectedIds = ids)
            }

    fun select(image: Gallery.Image, max: Int) {
        _selectedIds.update {
            it.toMutableList().apply {
                val result = remove(image.id)

                if (!result) {
                    //limit max size
                    if (_selectedIds.value.size < max)
                        add(image.id)
                }
            }
        }
    }

    private fun update(
        pagingData: PagingData<Gallery.Image>,
        selectedIds: List<Long>
    ): PagingData<Gallery.Image> {
        return pagingData.map {
            it.copy(
                selectedOrder = if (selectedIds.contains(it.id)) {
                    selectedIds.indexOf(it.id)
                } else it.selectedOrder
            )
        }
    }
}