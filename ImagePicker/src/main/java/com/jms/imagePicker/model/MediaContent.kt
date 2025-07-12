package com.jms.imagePicker.model

import android.net.Uri
import androidx.compose.runtime.Stable
import com.jms.imagePicker.Constants

@Stable
data class MediaContent(
    val id: Long,
    val title: String,
    val dateAt: Long,
    val data: String,
    val uri: Uri,
    val mimeType: String,
    val album: String?,
    val albumId: String?,
    val selectedOrder: Int = Constants.NO_ORDER,
    val selected: Boolean = false
)