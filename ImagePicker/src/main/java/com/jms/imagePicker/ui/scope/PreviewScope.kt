package com.jms.imagePicker.ui.scope

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Stable
import com.jms.imagePicker.model.MediaContent


@Stable
interface PreviewScope : BoxScope {
    fun onBack()
    fun onClick(mediaContent: MediaContent)
}


internal class PreviewScopeImpl(
    private val boxScope: BoxScope,
    private val onClick : (MediaContent) -> Unit = {},
    private val onBack: () -> Unit = {}
) : PreviewScope, BoxScope by boxScope {
    override fun onBack() {
        onBack.invoke()
    }

    override fun onClick(mediaContent: MediaContent) {
        onClick.invoke(mediaContent)
    }
}