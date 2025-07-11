package com.jms.imagePicker.ui.action

import androidx.compose.runtime.Stable
import com.jms.imagePicker.model.MediaContent

@Stable
interface PreviewActions {
    fun onBack()
    fun onClick(mediaContent: MediaContent)
}