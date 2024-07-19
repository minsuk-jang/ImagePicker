package com.jms.galleryselector.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.jms.galleryselector.data.LocalGalleryDataSource
import com.jms.galleryselector.manager.FileManager
import com.jms.galleryselector.model.Album
import com.jms.galleryselector.model.Gallery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import java.io.File

internal class GalleryScreenViewModel constructor(
    private val fileManager: FileManager,
    val localGalleryDataSource: LocalGalleryDataSource
) : ViewModel() {
    //selected gallery ids
    private val _selectedImages = MutableStateFlow<List<Gallery.Image>>(mutableListOf())
    val selectedImages: StateFlow<List<Gallery.Image>> = _selectedImages.asStateFlow()

    private val _albums: MutableStateFlow<List<Album>> = MutableStateFlow(mutableListOf())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    private val _selectedAlbum: MutableStateFlow<Album> = MutableStateFlow(Album(id = null))
    val selectedAlbum: StateFlow<Album> = _selectedAlbum.asStateFlow()

    val contents: Flow<PagingData<Gallery.Image>> = _selectedAlbum.flatMapLatest {
        localGalleryDataSource.getLocalGalleryImages(
            page = 1,
            albumId = it.id
        )
    }
        .cachedIn(viewModelScope)
        .combine(_selectedImages) { data, images ->
            update(pagingData = data, selectedImages = images)
        }

    private var _imageFile: File? = null

    init {
        getAlbums()
        setSelectedAlbum(album = _albums.value[0])
    }

    private fun getAlbums() {
        _albums.update {
            localGalleryDataSource.getAlbums()
        }
    }

    fun setSelectedAlbum(album: Album) {
        _selectedAlbum.update { album }
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
            fileManager.deleteImageFile(file = _imageFile!!)

            if (autoSelectAfterCapture)
                select(image = localGalleryDataSource.getLocalGalleryImage(), max = max)
        }
    }

    fun refresh() {
        refreshAlbum()
    }

    private fun refreshAlbum() {
        getAlbums()
        val newAlbum = _albums.value.first { it.id == _selectedAlbum.value.id }
        setSelectedAlbum(album = newAlbum)
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