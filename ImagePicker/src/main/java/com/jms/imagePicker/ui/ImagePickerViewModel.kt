package com.jms.imagePicker.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.jms.imagePicker.data.LocalMediaContentsDataSource
import com.jms.imagePicker.model.Action
import com.jms.imagePicker.model.Album
import com.jms.imagePicker.model.MediaContent
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.max
import kotlin.math.min

internal class ImagePickerViewModel(
    private val localMediaContentsDataSource: LocalMediaContentsDataSource
) : ViewModel() {
    private val selectionMutex = Mutex()

    private var dragSnapshot: Set<Uri> = emptySet()
    private var dragAction: Action = Action.ADD

    private val _selectedUris: MutableStateFlow<List<Uri>> = MutableStateFlow(listOf())
    val selectedUris: StateFlow<List<Uri>> = _selectedUris.asStateFlow()

    private val _selectedMediaContents =
        MutableStateFlow<List<MediaContent>>(emptyList())
    val selectedMediaContents: StateFlow<List<MediaContent>> = _selectedMediaContents.asStateFlow()

    private val _albums: MutableStateFlow<List<Album>> = MutableStateFlow(mutableListOf())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    private val _selectedAlbum: MutableStateFlow<Album?> = MutableStateFlow(null)
    val selectedAlbum: StateFlow<Album?> = _selectedAlbum.asStateFlow()

    val mediaContents: Flow<PagingData<MediaContent>> =
        _selectedAlbum.flatMapLatest {
            localMediaContentsDataSource.getMediaContents(
                albumId = it?.id
            )
        }.cachedIn(viewModelScope)
            .combine(_selectedUris) { data, uris ->
                markSelectedItems(pagingData = data, uris = uris)
            }.flowOn(Dispatchers.Default)

    init {
        initializeAlbum()
        observeSelectedUris()
    }

    private fun initializeAlbum() {
        viewModelScope.launch(Dispatchers.IO) {
            val albums = localMediaContentsDataSource.getAlbums()

            _albums.update { albums }
            _selectedAlbum.update { albums.getOrNull(0) }
        }
    }

    fun selectedAlbum(album: Album) {
        _selectedAlbum.update { album }
    }

    fun select(uri: Uri, max: Int) {
        viewModelScope.launch {
            selectionMutex.withLock {
                val current = _selectedUris.value.toMutableList()
                val index = current.indexOfFirst { it == uri }
                if (index == -1) {
                    if (current.size < max) current.add(uri)
                } else {
                    current.removeAt(index)
                }
                _selectedUris.update { current }
            }
        }
    }

    fun startDrag(uri: Uri, max: Int) {
        viewModelScope.launch {
            selectionMutex.withLock {
                val current = _selectedUris.value.toMutableList()
                val index = current.indexOfFirst { it == uri }
                if (index == -1) {
                    if (current.size < max) {
                        current.add(uri)
                        dragAction = Action.ADD
                    }
                } else {
                    current.removeAt(index)
                    dragAction = Action.REMOVE
                }
                dragSnapshot = current.toSet()
                _selectedUris.update { current }
            }
        }
    }

    fun updateDragSelection(start: Int, end: Int, mediaContents: List<MediaContent>, max: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            selectionMutex.withLock {
                val startIndex = (min(start, end) - 1).coerceAtLeast(0)
                val endIndex = max(start, end).coerceAtMost(mediaContents.size)
                val range = if (start <= end) {
                    mediaContents.subList(startIndex, endIndex)
                } else {
                    mediaContents.subList(startIndex, endIndex).reversed()
                }

                val newList = dragSnapshot.toMutableList()
                range.forEach { item ->
                    when (dragAction) {
                        Action.ADD -> if (!dragSnapshot.contains(item.uri) && newList.size < max) {
                            newList.add(item.uri)
                        }
                        Action.REMOVE -> newList.remove(item.uri)
                    }
                }

                _selectedUris.update { newList }
            }
        }
    }

    fun endDrag() {
        viewModelScope.launch {
            selectionMutex.withLock {
                dragSnapshot = emptySet()
            }
        }
    }

    private fun observeSelectedUris() {
        viewModelScope.launch(Dispatchers.Default) {
            _selectedUris.collectLatest { uris ->
                val newList = localMediaContentsDataSource.getMediaContents(uris)
                    .mapIndexed { index, item ->
                        item.copy(selectedOrder = index, selected = true)
                    }
                _selectedMediaContents.update { newList.toMutableList() }
            }
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