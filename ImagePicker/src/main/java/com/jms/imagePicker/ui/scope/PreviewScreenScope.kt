package com.jms.imagePicker.ui.scope

import androidx.compose.runtime.Stable
import com.jms.imagePicker.model.MediaContent


@Stable
interface PreviewScreenScope {
    /** Currently displayed media content. */
    val mediaContent: MediaContent

    /** Navigate back to the previous screen. */
    fun onBack()

    /**
     *
     * Called when a media content is selected or deselected
     *
     * @param mediaContent: MediaContent
     */
    fun onToggleSelection(mediaContent: MediaContent)
}