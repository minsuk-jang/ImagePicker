package com.jms.imagePicker.ui.scope

import androidx.compose.runtime.Stable
import com.jms.imagePicker.model.Gallery


@Stable
interface PreviewTopBarScope {
    val selectedImages: List<Gallery>
    fun onClick(image: Gallery)
}