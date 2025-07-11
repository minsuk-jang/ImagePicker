package com.jms.imagePicker.ui.scope

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Stable
import com.jms.imagePicker.model.MediaContent
import com.jms.imagePicker.ui.ImagePickerViewModel
import com.jms.imagePicker.ui.picker.ImagePickerState
import kotlin.math.max


@Stable
interface PreviewScope : BoxScope {
    fun onBack()
    fun onClick(mediaContent: MediaContent)
}


internal class PreviewScopeImpl(
    private val boxScope: BoxScope,
    private val viewModel: ImagePickerViewModel,
    private val state: ImagePickerState
) : PreviewScope, BoxScope by boxScope {
    override fun onBack() {

    }

    override fun onClick(mediaContent: MediaContent) {
        viewModel.select(uri = mediaContent.uri, max = state.max)
    }
}