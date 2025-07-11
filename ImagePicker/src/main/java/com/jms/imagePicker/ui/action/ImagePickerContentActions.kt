package com.jms.imagePicker.ui.action

import androidx.compose.runtime.Stable
import com.jms.imagePicker.model.MediaContent


@Stable
interface ImagePickerContentActions {
    fun onClick(mediaContent: MediaContent)
    fun onNavigateToPreview(mediaContent: MediaContent)
}