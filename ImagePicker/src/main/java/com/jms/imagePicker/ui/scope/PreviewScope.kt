package com.jms.imagePicker.ui.scope

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Stable
import com.jms.imagePicker.model.MediaContent


@Stable
interface PreviewScope {
    val mediaContent: MediaContent
}


internal class PreviewScopeImpl(
    private val boxScope: BoxScope,
    override val mediaContent: MediaContent
) : PreviewScope, BoxScope by boxScope