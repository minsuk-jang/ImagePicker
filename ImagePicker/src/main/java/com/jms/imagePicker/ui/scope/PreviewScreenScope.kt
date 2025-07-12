package com.jms.imagePicker.ui.scope

import androidx.compose.runtime.Stable
import com.jms.imagePicker.model.MediaContent


@Stable
interface PreviewScreenScope  {
    val mediaContent: MediaContent

    fun onBack()
    fun onClick(mediaContent: MediaContent)
}