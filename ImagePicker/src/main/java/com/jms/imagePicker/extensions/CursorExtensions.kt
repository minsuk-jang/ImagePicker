package com.jms.imagePicker.extensions

import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.jms.imagePicker.model.MediaContent


/**
 *
 * get column string
 */
internal fun Cursor.getColumnString(index: String): String? {
    val columnIndex = getColumnIndex(index)
    return if (columnIndex != -1)
        getString(columnIndex)
    else null
}


internal fun Cursor.toImage(): MediaContent {
    val id = getLong(getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
    val title =
        getString(getColumnIndexOrThrow(MediaStore.MediaColumns.TITLE))
    val dateAt =
        getLong(getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED))

    val mimeType =
        getString(getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE))

    val uri = Uri.withAppendedPath(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        id.toString()
    )

    val data =
        getString(getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))

    val album =
        getColumnString(index = MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
    val albumId = getColumnString(index = MediaStore.MediaColumns.BUCKET_ID)

    return MediaContent(
        id = id,
        title = title,
        dateAt = dateAt,
        data = data,
        uri = uri,
        mimeType = mimeType,
        album = album,
        albumId = albumId
    )
}
