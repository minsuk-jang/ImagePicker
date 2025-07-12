package com.jms.imagePicker.ui.action.picker

import androidx.compose.runtime.Stable
import com.jms.imagePicker.model.Album


@Stable
interface ImagePickerAlbumActions {
    fun onSelect(album: Album)
}