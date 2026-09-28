package com.jms.imagePicker.manager

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.jms.imagePicker.model.MediaSeekKey

internal class API21MediaContentManager(
    private val context: Context
) : MediaContentManager() {
    override val contentResolver: ContentResolver get() = context.contentResolver

    override fun getCursor(
        uri: Uri,
        projection: Array<String>,
        albumId: String?,
        seekKey: MediaSeekKey?,
        limit: Int
    ): Cursor? {
        val albumClause = if (albumId != null) " AND ${MediaStore.MediaColumns.BUCKET_ID} = ?" else ""
        val seekClause = if (seekKey != null) {
            " AND (${MediaStore.Files.FileColumns.DATE_MODIFIED} < ? OR " +
                    "(${MediaStore.Files.FileColumns.DATE_MODIFIED} = ? AND ${MediaStore.MediaColumns._ID} < ?))"
        } else ""
        val selectionClause = baseSelectionClause + albumClause + seekClause

        val selectionArgs = baseSelectionArgs.toMutableList().apply {
            albumId?.let { add(it) }
            seekKey?.let {
                add(it.dateModified.toString())
                add(it.dateModified.toString())
                add(it.id.toString())
            }
        }.toTypedArray()

        return context.contentResolver.query(
            uri,
            projection,
            selectionClause,
            selectionArgs,
            "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC, ${MediaStore.MediaColumns._ID} DESC LIMIT $limit"
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