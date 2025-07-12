package com.jms.imagePicker.ui.scope

import androidx.compose.runtime.Stable
import com.jms.imagePicker.model.MediaContent


@Stable
interface ImagePickerContentScope {
    val mediaContent : MediaContent
    fun onNavigateToPreview(mediaContent: MediaContent)
}