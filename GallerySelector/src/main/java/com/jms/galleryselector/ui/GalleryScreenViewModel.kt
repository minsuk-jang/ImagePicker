package com.jms.galleryselector.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.jms.galleryselector.Constants.NO_ORDER
import com.jms.galleryselector.Constants.TAG
import com.jms.galleryselector.data.LocalGalleryDataSource
import com.jms.galleryselector.manager.FileManager
import com.jms.galleryselector.model.Action
import com.jms.galleryselector.model.Album
import com.jms.galleryselector.model.Gallery
import com.jms.galleryselector.model.OrderedUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.max
import kotlin.math.min

internal class GalleryScreenViewModel(
    private val fileManager: FileManager,
    val localGalleryDataSource: LocalGalleryDataSource
) : ViewModel() {
    //init action
    private var _initAction: Action = Action.ADD //add

    private val _selectedUris: MutableStateFlow<List<OrderedUri>> = MutableStateFlow(listOf())
    val selectedUris: StateFlow<List<Uri>> = _selectedUris
        .map {
            it.map { it.uri }.apply {
                Log.e(TAG, "$this ")
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, listOf())

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
            update(pagingData = data, orderUris = uris)
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
                val index = indexOfFirst { it.uri == uri }

                if (index == -1) {
                    //limit max size
                    if (_selectedUris.value.size < max) {
                        add(OrderedUri(order = _selectedUris.value.size, uri = uri, prev = true))
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

    /**
     *
     * previousUris, currentUris
     * viewUris = combine(previousUris, currentUris)
     *
     * inter = previousUris, currentUris intersection
     * previousUris - inter
     *
     * Move inter items to the front in currentUris
     *
     * then finally set previousUris + currentUris
     */
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
                val startIndex = (min(start, end) - 1).coerceAtLeast(0)
                val endIndex = max(start, end)
                val tempList = images.subList(startIndex, endIndex)

                //selected && not contain current list
                val prevList = _selectedUris.value.filter { uri ->
                    uri.prev && !tempList.any { it.uri == uri.uri }
                }

                val newList = when (start < end) {
                    true -> images.subList(startIndex, endIndex).mapIndexed { index, image ->
                        OrderedUri(
                            order = when (image.selectedOrder == NO_ORDER) {
                                true -> prevList.lastIndex + index
                                false -> image.selectedOrder
                            },
                            uri = image.uri,
                            prev = false
                        )
                    }

                    false -> images.subList(startIndex, endIndex).reversed()
                        .mapIndexed { index, image ->
                            OrderedUri(
                                order = when (image.selectedOrder == NO_ORDER) {
                                    true -> prevList.lastIndex + index
                                    false -> image.selectedOrder
                                },
                                uri = image.uri,
                                prev = false
                            )
                        }
                }

                _selectedUris.update { prevList + newList }
            }
        }
    }

    fun synchronize() {
        viewModelScope.launch(Dispatchers.Default) {
            _selectedUris.update {
                it.map {
                    it.copy(prev = true)
                }
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            val newList = buildList {
                selectedUris.value.forEach {
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
        orderUris: List<OrderedUri>
    ): PagingData<Gallery.Image> {
        return pagingData.map { image ->
            image.copy(
                selectedOrder = orderUris.firstOrNull { it.uri == image.uri }?.order ?: NO_ORDER,
                selected = orderUris.any { it.uri == image.uri }
            )
        }
    }
}