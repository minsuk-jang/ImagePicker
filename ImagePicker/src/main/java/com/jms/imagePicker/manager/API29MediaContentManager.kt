package com.jms.imagePicker.manager

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf


@RequiresApi(Build.VERSION_CODES.R)
internal class API29MediaContentManager(
    private val context: Context
) : MediaContentManager() {
    override fun getCursor(
        uri: Uri,
        projection: Array<String>,
        albumId: String?,
        offset: Int,
        limit: Int
    ): Cursor? {
        val selection = baseSelectionClause + " AND ${MediaStore.MediaColumns.IS_PENDING} = ?" +
                if (albumId != null) " AND ${MediaStore.MediaColumns.BUCKET_ID} = ?" else ""
        val selectionArgs = baseSelectionArgs.toMutableList().apply {
            add("0")
            albumId?.let { add(it) }
        }.toTypedArray()

        val selectionBundle = bundleOf(
            ContentResolver.QUERY_ARG_OFFSET to offset,
            ContentResolver.QUERY_ARG_LIMIT to limit,
            ContentResolver.QUERY_ARG_SORT_COLUMNS to arrayOf(
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.MediaColumns._ID
            ),
            ContentResolver.QUERY_ARG_SORT_DIRECTION to ContentResolver.QUERY_SORT_DIRECTION_DESCENDING,
            ContentResolver.QUERY_ARG_SQL_SELECTION to selection,
            ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to selectionArgs
        )

        return context.contentResolver.query(uri, projection, selectionBundle, null)
    }

    override fun getAlbumCursor(uri: Uri, projection: Array<String>): Cursor? {
        val selectionBundle = bundleOf(
            ContentResolver.QUERY_ARG_SQL_SELECTION to baseSelectionClause,
            ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to baseSelectionArgs.toTypedArray()
        )
        return context.contentResolver.query(uri, projection, selectionBundle, null)
    }

    override fun getCursorByIds(uri: Uri, projection: Array<String>, ids: List<Long>): Cursor? {
        if (ids.isEmpty()) return null
        val placeholders = ids.joinToString(",") { "?" }
        val selection = "$baseSelectionClause AND ${MediaStore.MediaColumns._ID} IN ($placeholders)"
        val selectionArgs = (baseSelectionArgs + ids.map { it.toString() }).toTypedArray()
        return context.contentResolver.query(uri, projection, selection, selectionArgs, null)
    }
}