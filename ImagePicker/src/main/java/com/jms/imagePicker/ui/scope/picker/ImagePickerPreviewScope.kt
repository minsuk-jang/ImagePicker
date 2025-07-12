package com.jms.imagePicker.ui.scope.picker

import androidx.compose.runtime.Stable
import com.jms.imagePicker.model.MediaContent


@Stable
interface ImagePickerPreviewScope {
    /** The list of media contents currently selected. */
    val selectedMediaContents: List<MediaContent>

    /**
     * Deselect media content
     */
    fun onDeselect(mediaContent: MediaContent)
}