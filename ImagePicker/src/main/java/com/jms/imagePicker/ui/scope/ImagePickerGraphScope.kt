package com.jms.imagePicker.ui.scope

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.jms.imagePicker.ui.scope.picker.ImagePickerAlbumScope
import com.jms.imagePicker.ui.scope.picker.ImagePickerPreviewTopBarScope


@Stable
interface ImagePickerGraphScope {
    fun ImagePickerScreen(
        albumTopBar: @Composable ImagePickerAlbumScope.() -> Unit = {},
        previewTopBar: @Composable ImagePickerPreviewTopBarScope.() -> Unit = {},
        content: @Composable ImagePickerCellScope.() -> Unit = {}
    )

    /**
     *
     * @param content: Preview Content UI Composable.
     */
    fun PreviewScreen(
        content: @Composable PreviewScreenScope.() -> Unit = { }
    )
}