package com.jms.imagePicker.ui.scope.picker

import androidx.compose.runtime.Stable
import com.jms.imagePicker.model.MediaContent


@Stable
interface ImagePickerPreviewTopBarScope {
    /** The list of media contents currently selected. */
    val selectedMediaContents: List<MediaContent>

    /**
     * Called when a selected media item in the preview top bar is clicked.
     */
    fun onClick(mediaContent: MediaContent)
}