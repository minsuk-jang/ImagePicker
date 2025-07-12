package com.jms.imagePicker.ui.scope

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Stable
import com.jms.imagePicker.model.MediaContent


@Stable
interface PreviewScope  {
    val mediaContent: MediaContent

    fun onBack()
    fun onClick(mediaContent: MediaContent)
}