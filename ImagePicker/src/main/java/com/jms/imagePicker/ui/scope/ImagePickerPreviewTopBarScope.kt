package com.jms.imagePicker.ui.scope

import androidx.compose.runtime.Stable
import com.jms.imagePicker.model.MediaContent


@Stable
interface ImagePickerPreviewTopBarScope {
    val selectedMediaContents: List<MediaContent>
    fun onClick(mediaContent: MediaContent)
}