package com.jms.imagePicker.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.jms.imagePicker.data.LocalMediaContentsDataSource
import com.jms.imagePicker.manager.FileManager
import com.jms.imagePicker.model.Action
import com.jms.imagePicker.model.Album
import com.jms.imagePicker.model.MediaContent
import com.jms.imagePicker.model.OrderedUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.max
import kotlin.math.min

internal class ImagePickerViewModel(
    private val fileManager: FileManager,
    private val localMediaContentsDataSource: LocalMediaContentsDataSource
) : ViewModel() {
    //init action
    private var _initAction: Action = Action.ADD //add

    private val _previousSelectedUris: LinkedHashSet<OrderedUri> = LinkedHashSet()

    private val _selectedUris: MutableStateFlow<List<Uri>> = MutableStateFlow(listOf())
    val selectedUris: StateFlow<List<Uri>> = _selectedUris.asStateFlow()

    private val _selectedMediaContents =
        MutableStateFlow<List<MediaContent>>(emptyList())
    val selectedMediaContents: StateFlow<List<MediaContent>> = _selectedMediaContents.asStateFlow()

    private val _albums: MutableStateFlow<List<Album>> = MutableStateFlow(mutableListOf())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    private val _selectedAlbum: MutableStateFlow<Album?> = MutableStateFlow(null)
    val selectedAlbum: StateFlow<Album?> = _selectedAlbum.asStateFlow()

    private val _refreshTrigger: MutableStateFlow<Long> = MutableStateFlow(0L)

    val mediaContents: Flow<PagingData<MediaContent>> =
        combine(_selectedAlbum, _refreshTrigger) { album, _ ->
            album
        }.flatMapLatest {
            localMediaContentsDataSource.getMediaContents(
                albumId = it?.id
            )
        }.cachedIn(viewModelScope)
            .combine(_selectedUris) { data, uris ->
                markSelectedItems(pagingData = data, uris = uris)
            }.flowOn(Dispatchers.Default)

    private var _imageFile: File? = null

    init {
        initializeAlbum()
        observeSelectedUris()
    }

    private fun initializeAlbum() {
        viewModelScope.launch {
            val albums = localMediaContentsDataSource.getAlbums()

            _albums.update { albums }
            _selectedAlbum.update { albums.getOrNull(0) }
        }
    }

    fun selectedAlbum(album: Album) {
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
                        _previousSelectedUris.add(
                            OrderedUri(
                                order = _selectedUris.value.size,
                                uri = uri
                            )
                        )

                        _initAction = Action.ADD
                    }
                } else {
                    removeAt(index)

                    val idx = _previousSelectedUris.indexOfFirst { it.uri == uri }
                    _previousSelectedUris.remove(_previousSelectedUris.elementAt(idx))

                    _initAction = Action.REMOVE
                }
            }
        }
    }

    fun select(
        start: Int?,
        end: Int?,
        mediaContents: List<MediaContent>,
        max: Int
    ) {
        if (start != null && end != null) {
            viewModelScope.launch(Dispatchers.Default) {
                val startIndex = (min(start, end) - 1).coerceAtLeast(0)
                val endIndex = max(start, end)

                val newList = buildList {
                    val tempList = when (start < end) {
                        true -> mediaContents.subList(startIndex, endIndex)
                        false -> mediaContents.subList(startIndex, endIndex).reversed()
                    }

                    addAll(_previousSelectedUris)

                    tempList.forEachIndexed { index, image ->
                        val idx = _previousSelectedUris.indexOfFirst { it.uri == image.uri }
                        if (idx != -1) {
                            //already selected
                            val uri = _previousSelectedUris.elementAt(idx)
                            _previousSelectedUris.remove(uri)

                            if (_initAction == Action.REMOVE) {
                                remove(uri)
                            }
                        } else {
                            //not contain
                            if (_initAction == Action.ADD && size < max) {
                                add(
                                    OrderedUri(
                                        order = when (image.selected) {
                                            true -> image.selectedOrder
                                            false -> size + index
                                        },
                                        uri = image.uri
                                    )
                                )
                            }
                        }
                    }
                }.sortedBy { it.order }.map { it.uri }

                _selectedUris.update { newList }
            }
        }
    }


    fun synchronize() {
        viewModelScope.launch(Dispatchers.Default) {
            _previousSelectedUris.clear()
            _previousSelectedUris.addAll(_selectedUris.value.mapIndexed { index, uri ->
                OrderedUri(
                    order = index,
                    uri = uri
                )
            })
        }
    }

    //TODO update performance
    private fun observeSelectedUris() {
        viewModelScope.launch(Dispatchers.Default) {
            _selectedUris.collectLatest { uris ->
                val newList = uris.mapIndexed { index, uri ->
                    val image = localMediaContentsDataSource.getMediaContent(uri).copy(
                        selectedOrder = index,
                        selected = true
                    )
                    image
                }

                _selectedMediaContents.update { newList.toMutableList() }
            }

        }
    }

    fun createImageFile(): File {
        _imageFile = fileManager.createImageFile()
        return _imageFile ?: throw IllegalStateException("File is null!!")
    }

    fun saveImageFile(max: Int, autoSelectAfterCapture: Boolean) {
        if (_imageFile != null) {
            viewModelScope.launch(Dispatchers.IO) {
                fileManager.saveImageFile(file = _imageFile!!)

                if (autoSelectAfterCapture) {
                    select(uri = localMediaContentsDataSource.getMediaContent().uri, max = max)
                }

                invalidateMediaContents()
                invalidateAlbum()
            }
        }
    }

    private fun invalidateMediaContents() {
        _refreshTrigger.update { System.currentTimeMillis() }
    }

    private fun invalidateAlbum() {
        viewModelScope.launch {
            val albums = localMediaContentsDataSource.getAlbums()
            _albums.update { albums }

            val newAlbum =
                _albums.value.firstOrNull { it.id == _selectedAlbum.value?.id } ?: return@launch
            selectedAlbum(album = newAlbum)
        }
    }

    private fun markSelectedItems(
        pagingData: PagingData<MediaContent>,
        uris: List<Uri>
    ): PagingData<MediaContent> {
        return pagingData.map { image ->
            image.copy(
                selectedOrder = uris.indexOfFirst { it == image.uri },
                selected = uris.any { it == image.uri }
            )
        }
    }
}