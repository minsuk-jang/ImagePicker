package com.jms.imagePicker.ui

import androidx.lifecycle.ViewModel
import com.jms.imagePicker.model.Album
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MainActivityUiModel(
    val albums: List<Album> = emptyList(),
    val selectedAlbum: Album? = null
)

class MainActivityViewModel : ViewModel() {
    private val _uiModel: MutableStateFlow<MainActivityUiModel> = MutableStateFlow(
        MainActivityUiModel()
    )
    val uiModel: StateFlow<MainActivityUiModel> = _uiModel.asStateFlow()

    fun setAlbums(albums: List<Album>) {
        _uiModel.update {
            it.copy(
                albums = albums
            )
        }
    }

    fun selectAlbum(album: Album) {
        _uiModel.update {
            it.copy(
                selectedAlbum = album
            )
        }
    }
}