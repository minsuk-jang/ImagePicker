package com.jms.imagePicker.ui.scope

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.jms.imagePicker.model.MediaContent
import com.jms.imagePicker.ui.action.ImagePickerContentActions
import com.jms.imagePicker.ui.action.PreviewActions


@Stable
interface ImagePickerGraphScope {
    val selectedMediaContents: List<MediaContent>

    fun ImagePickerScreen(
        albumTopBar: @Composable ImagePickerAlbumScope.() -> Unit = {},
        previewTopBar: @Composable ImagePickerPreviewTopBarScope.() -> Unit = {},
        content: @Composable BoxScope.(ImagePickerContentActions, MediaContent) -> Unit = { _, _ -> }
    )

    /**
     *
     * @param content: Preview Content UI Composable.
     */
    fun PreviewScreen(
        content: @Composable BoxScope.(PreviewActions, MediaContent) -> Unit = { _, _ -> }
    )
}