package com.jms.imagePicker.manager

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

internal class API21MediaContentManager(
    private val context: Context
) : MediaContentManager() {
    override fun getCursor(
        uri: Uri,
        projection: Array<String>,
        albumId: String?,
        offset: Int,
        limit: Int
    ): Cursor? {
        val selectionClause = baseSelectionClause +
                if (albumId != null) " AND ${MediaStore.MediaColumns.BUCKET_ID} = ?" else ""

        val selectionArgs = baseSelectionArgs.toMutableList().apply {
            albumId?.let { add(it) }
        }.toTypedArray()

        return context.contentResolver.query(
            uri,
            projection,
            selectionClause,
            selectionArgs,
            "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC LIMIT $limit OFFSET $offset"
        )
    }

    override fun getAlbumCursor(uri: Uri, projection: Array<String>): Cursor? {
        return context.contentResolver.query(
            uri,
            projection,
            baseSelectionClause,
            baseSelectionArgs.toTypedArray(),
            null
        )
    }

    override fun getCursorByIds(uri: Uri, projection: Array<String>, ids: List<Long>): Cursor? {
        if (ids.isEmpty()) return null
        val placeholders = ids.joinToString(",") { "?" }
        val selection = "$baseSelectionClause AND ${MediaStore.MediaColumns._ID} IN ($placeholders)"
        val selectionArgs = (baseSelectionArgs + ids.map { it.toString() }).toTypedArray()
        return context.contentResolver.query(uri, projection, selection, selectionArgs, null)
    }
}