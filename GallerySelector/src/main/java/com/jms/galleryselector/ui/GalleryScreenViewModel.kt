package com.jms.galleryselector.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.jms.galleryselector.Constants
import com.jms.galleryselector.Constants.TAG
import com.jms.galleryselector.data.LocalGalleryDataSource
import com.jms.galleryselector.manager.FileManager
import com.jms.galleryselector.model.Action
import com.jms.galleryselector.model.Album
import com.jms.galleryselector.model.Gallery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.LinkedHashSet
import kotlin.math.max
import kotlin.math.min

internal class GalleryScreenViewModel(
    private val fileManager: FileManager,
    val localGalleryDataSource: LocalGalleryDataSource
) : ViewModel() {

    //init action
    private var _initAction: Action = Action.ADD //add

    //selected gallery ids
    private val _selectedUris = MutableStateFlow<List<Uri>>(mutableListOf())
    val selectedUris: StateFlow<List<Uri>> = _selectedUris.asStateFlow()

    private val _selectedImages = MutableStateFlow<MutableList<Gallery.Image>>(mutableListOf())
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
    }.cachedIn(viewModelScope)
        .combine(_selectedUris) { data, uris ->
            update(pagingData = data, selectedUris = uris)
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

    fun select(uri: Uri, max: Int) {
        _selectedUris.update {
            it.toMutableList().apply {
                val index = indexOfFirst { it == uri }

                if (index == -1) {
                    //limit max size
                    if (_selectedUris.value.size < max) {
                        add(uri)
                        _initAction = Action.ADD
                        performInternalAdditional(uri = uri)
                    }
                } else {
                    removeAt(index)
                    _initAction = Action.REMOVE
                    performInternalRemoval(uri = uri)
                }
            }
        }
    }

    fun select(
        start: Int?,
        middle: Int?,
        end: Int?,
        pivot: Int?,
        curRow: Int?,
        prevRow: Int?,
        images: List<Gallery.Image>,
        max: Int
    ) {
        if (start != null && middle != null && end != null && pivot != null && curRow != null && prevRow != null) {
            viewModelScope.launch(Dispatchers.Default) {
                val startIndex = (min(middle, end)).coerceAtLeast(0)
                val endIndex = max(middle, end)

                val isUpward = pivot > curRow

                val new = when (isUpward) {
                    true -> images.subList(startIndex, endIndex).reversed()
                    false -> images.subList(startIndex, endIndex)
                }

                val newList = buildList {
                    addAll(_selectedUris.value)

                    new.forEach {
                        when (isUpward) {
                            true -> {
                                val isInstantUpward = curRow < prevRow
                                when (isInstantUpward) {
                                    true -> {
                                        //up
                                        if (_initAction == Action.ADD
                                            && max > size
                                            && !_selectedUris.value.contains(it.uri)
                                        ) {
                                            add(it.uri)
                                        }
                                    }

                                    false -> {
                                        //down
                                        when (_initAction) {
                                            Action.ADD -> remove(it.uri)
                                            Action.REMOVE -> {
                                                if (max > size
                                                    && !_selectedUris.value.contains(it.uri)
                                                ) {
                                                    add(it.uri)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            false -> {
                                val isInstantDownward = curRow > prevRow
                                when (isInstantDownward) {
                                    true -> {
                                        //down
                                        if (_initAction == Action.ADD
                                            && max > size
                                            && !_selectedUris.value.contains(it.uri)
                                        ) {
                                            add(it.uri)
                                        }
                                    }

                                    false -> {
                                        //up
                                        when (_initAction) {
                                            Action.ADD -> remove(it.uri)
                                            Action.REMOVE -> {
                                                if (max > size
                                                    && !_selectedUris.value.contains(it.uri)
                                                ) {
                                                    add(it.uri)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                _selectedUris.update { newList }
            }
        }
    }

    fun synchronize() {
        viewModelScope.launch(Dispatchers.Default) {
            val newList = buildList {
                _selectedUris.value.forEach {
                    val image = localGalleryDataSource.getLocalGalleryImage(it)
                    add(image)
                }
            }

            _selectedImages.update { newList.toMutableList() }
        }
    }

    private fun performInternalAdditional(uri: Uri) {
        viewModelScope.launch(Dispatchers.Default) {
            val image = localGalleryDataSource.getLocalGalleryImage(uri = uri)
            val newList = _selectedImages.value.toMutableList().apply { add(image) }
            _selectedImages.update { newList }
        }
    }

    private fun performInternalRemoval(uri: Uri) {
        viewModelScope.launch(Dispatchers.Default) {
            val index = _selectedImages.value.indexOfFirst { it.uri == uri }
            if (index != -1) {
                val newList = _selectedImages.value.toMutableList().apply {
                    removeAt(index)
                }

                _selectedImages.update { newList }
            }
        }
    }

    fun createImageFile(): File {
        _imageFile = fileManager.createImageFile()
        return _imageFile ?: throw IllegalStateException("File is null!!")
    }

    fun saveImageFile(context: Context, max: Int, autoSelectAfterCapture: Boolean) {
        if (_imageFile != null) {
            viewModelScope.launch(Dispatchers.IO) {
                fileManager.saveImageFile(context = context, file = _imageFile!!)

                if (autoSelectAfterCapture)
                    select(uri = localGalleryDataSource.getLocalGalleryImage().uri, max = max)
            }
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

    private fun update(
        pagingData: PagingData<Gallery.Image>,
        selectedUris: List<Uri>
    ): PagingData<Gallery.Image> {
        return pagingData.map { image ->
            image.copy(
                selectedOrder = selectedUris.indexOfFirst { it == image.uri },
                selected = selectedUris.any { it == image.uri }
            )
        }
    }
}