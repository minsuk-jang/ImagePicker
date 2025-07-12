package com.jms.imagePicker.ui.scope.picker

import androidx.compose.runtime.Stable
import com.jms.imagePicker.model.Album

@Stable
interface ImagePickerAlbumScope {
    val albums: List<Album>
    val selectedAlbum: Album?
}