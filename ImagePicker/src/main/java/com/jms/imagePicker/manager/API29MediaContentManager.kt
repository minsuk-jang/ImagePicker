package com.jms.imagePicker.manager

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import com.jms.imagePicker.model.MediaSeekKey


@RequiresApi(Build.VERSION_CODES.R)
internal class API29MediaContentManager(
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
        val selection = baseSelectionClause + " AND ${MediaStore.MediaColumns.IS_PENDING} = ?" +
                albumClause + seekClause
        val selectionArgs = baseSelectionArgs.toMutableList().apply {
            add("0")
            albumId?.let { add(it) }
            seekKey?.let {
                add(it.dateModified.toString())
                add(it.dateModified.toString())
                add(it.id.toString())
            }
        }.toTypedArray()

        val selectionBundle = bundleOf(
            ContentResolver.QUERY_ARG_LIMIT to limit,
            ContentResolver.QUERY_ARG_SQL_SORT_ORDER to
                    "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC, ${MediaStore.MediaColumns._ID} DESC",
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