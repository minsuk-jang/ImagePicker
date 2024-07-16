package com.jms.galleryselector.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.jms.galleryselector.data.LocalGalleryDataSource
import com.jms.galleryselector.manager.FileManager
import com.jms.galleryselector.model.Gallery
import com.jms.galleryselector.model.toImage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.io.File

internal class GalleryScreenViewModel constructor(
    private val fileManager: FileManager,
    val localGalleryDataSource: LocalGalleryDataSource
) : ViewModel() {
    //selected gallery ids
    private val _selectedImages = MutableStateFlow<List<Gallery.Image>>(mutableListOf())
    val selectedImages: StateFlow<List<Gallery.Image>> = _selectedImages.asStateFlow()

    private var _imageFile: File? = null

    //album
    private val _selectedAlbumId : MutableStateFlow<String?> = MutableStateFlow(null)

    init {
        getAlbums()
    }

    private fun getAlbums() {
        val list = localGalleryDataSource.getAlbums()
        Log.e("jms8732", "getAlbums: $list")
    }

    fun getGalleryContents(
        page: Int = 1
    ): Flow<PagingData<Gallery.Image>> {
        return _selectedAlbumId.flatMapLatest { albumId ->
            localGalleryDataSource.getLocalGalleryImages(
                page = page,
                albumId = albumId
            )
        }.map {
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

    fun createImageFile(): File {
        _imageFile = fileManager.createImageFile()
        return _imageFile ?: throw IllegalStateException("File is null!!")
    }

    fun saveImageFile(context: Context, max: Int, autoSelectAfterCapture: Boolean) {
        if (_imageFile != null) {
            fileManager.saveImageFile(context = context, file = _imageFile!!)

            if (autoSelectAfterCapture)
                select(image = localGalleryDataSource.getLocalGalleryImage().toImage(), max = max)
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