package com.jms.imagePicker.ui.scope

import androidx.compose.runtime.Stable
import com.jms.imagePicker.model.MediaContent


@Stable
interface ImagePickerCellScope {
    /** The media content represented by this cell */
    val mediaContent : MediaContent

    /**
     * Navigate to the preview screen for the given media content.
     *
     * **Caution**
     * This function only valid if you have declared `PreviewScreen` inside `ImagePickerNavHost`.
     * Make sure the preview screen is part of the graph.
     *
     * Calling this without declaring a `PreviewScreen` in `ImagePickerNavHost` will result in a runtime exception.
     *
     * @param mediaContent: The media item to preview
     */
    fun onNavigateToPreviewScreen(mediaContent: MediaContent)
}