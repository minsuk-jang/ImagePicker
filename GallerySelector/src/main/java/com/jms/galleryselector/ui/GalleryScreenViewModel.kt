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
import com.jms.galleryselector.model.Album
import com.jms.galleryselector.model.Gallery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import java.io.File

internal class GalleryScreenViewModel(
    private val fileManager: FileManager,
    val localGalleryDataSource: LocalGalleryDataSource
) : ViewModel() {
    //selected gallery ids
    private val _selectedIds = MutableStateFlow<List<Long>>(mutableListOf())
    val selectedIds: StateFlow<List<Long>> = _selectedIds.asStateFlow()

    private val _albums: MutableStateFlow<List<Album>> = MutableStateFlow(mutableListOf())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    private val _selectedAlbum: MutableStateFlow<Album> = MutableStateFlow(Album(id = null))
    val selectedAlbum: StateFlow<Album> = _selectedAlbum.asStateFlow()

    val contents: Flow<PagingData<Gallery.Image>> = _selectedAlbum.flatMapLatest {
        localGalleryDataSource.getLocalGalleryImages(
            page = 1,
            albumId = it.id
        )
    }.cachedIn(viewModelScope)
        .combine(_selectedIds) { data, images ->
            update(pagingData = data, selectedIds = images)
        }.flowOn(Dispatchers.Default)

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
        _selectedIds.update {
            it.toMutableList().apply {
                val index = indexOfFirst { it == image.id }

                if (index == -1) {
                    //limit max size
                    if (_selectedIds.value.size < max)
                        add(image.id)
                } else {
                    removeAt(index)
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
        selectedIds: List<Long>
    ): PagingData<Gallery.Image> {
        return pagingData.map { image ->
            image.copy(
                selectedOrder = selectedIds.indexOfFirst { it == image.id },
                selected = selectedIds.any { it == image.id }
            )
        }
    }
}