package com.jms.galleryselector.manager

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.os.bundleOf

internal class MediaContentManager(
    private val context: Context
) {
    fun getCursor(
        uri: Uri,
        offset: Int,
        limit: Int,
    ): Cursor? {
        //filter deleted media contents
        val selection = MediaStore.MediaColumns.IS_PENDING + " = ?"
        val selectionArgs = arrayOf("0")
        val projection = arrayOf(
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.TITLE,
            MediaStore.Images.ImageColumns.DATE_MODIFIED,
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.MIME_TYPE,
            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Images.ImageColumns.BUCKET_ID
        )

        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            val selectionBundle = bundleOf(
                ContentResolver.QUERY_ARG_OFFSET to offset,
                ContentResolver.QUERY_ARG_LIMIT to limit,
                ContentResolver.QUERY_ARG_SORT_COLUMNS to arrayOf(
                    MediaStore.Files.FileColumns.DATE_MODIFIED
                ),
                ContentResolver.QUERY_ARG_SORT_DIRECTION to ContentResolver.QUERY_SORT_DIRECTION_DESCENDING,
                ContentResolver.QUERY_ARG_SQL_SELECTION to selection,
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to selectionArgs
            )

            context.contentResolver.query(
                uri,
                projection,
                selectionBundle,
                null
            )
        } else {
            context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC LIMIT $limit OFFSET $offset"
            )
        }
    }
}