package com.jms.galleryselector.model

import android.net.Uri
import com.jms.galleryselector.Constants

sealed interface Gallery {

    data class Image(
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
    ) : Gallery
}