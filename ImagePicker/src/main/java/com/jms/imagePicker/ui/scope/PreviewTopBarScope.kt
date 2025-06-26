package com.jms.imagePicker.ui.scope

import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jms.imagePicker.model.Gallery


@Stable
interface PreviewTopBarScope {
    val selectedImages: SnapshotStateList<Gallery.Image>
    fun onClick(image: Gallery)
}