package com.jms.imagePicker.ui.scope.picker

import androidx.compose.runtime.Stable
import com.jms.imagePicker.model.Album


@Stable
interface ImagePickerAlbumScope {
    /** The list of available local albums */
    val albums: List<Album>

    /** Currently selected album, or null if no album is selected */
    val selectedAlbum: Album?

    /**
     *
     * Called when an album is selected by the user
     */
    fun onClick(album: Album)
}