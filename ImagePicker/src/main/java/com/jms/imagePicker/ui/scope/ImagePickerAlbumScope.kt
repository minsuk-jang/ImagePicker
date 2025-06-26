package com.jms.imagePicker.ui.scope

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jms.imagePicker.model.Album

@Stable
interface ImagePickerAlbumScope {
    val albums: List<Album>
    val selectedAlbum: Album?
    fun onSelect(album: Album)
}