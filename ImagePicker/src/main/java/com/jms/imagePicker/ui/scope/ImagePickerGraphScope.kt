package com.jms.imagePicker.ui.scope

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.jms.imagePicker.model.MediaContent
import com.jms.imagePicker.ui.action.picker.ImagePickerAlbumActions
import com.jms.imagePicker.ui.action.picker.ImagePickerContentActions
import com.jms.imagePicker.ui.action.PreviewActions
import com.jms.imagePicker.ui.scope.picker.ImagePickerAlbumScope
import com.jms.imagePicker.ui.scope.picker.ImagePickerPreviewTopBarScope


@Stable
interface ImagePickerGraphScope {
    fun ImagePickerScreen(
        albumTopBar: @Composable ImagePickerAlbumScope.(ImagePickerAlbumActions) -> Unit = {},
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