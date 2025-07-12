package com.jms.imagePicker.ui.action.picker

import androidx.compose.runtime.Stable
import com.jms.imagePicker.model.MediaContent


@Stable
interface ImagePickerContentActions {
    fun onNavigateToPreview(mediaContent: MediaContent)
}