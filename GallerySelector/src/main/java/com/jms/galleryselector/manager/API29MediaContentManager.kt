package com.jms.galleryselector.manager

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf


@RequiresApi(Build.VERSION_CODES.O)
internal class API29MediaContentManager(
    private val context: Context
) : MediaContentManager(context = context) {

    override fun getCursor(
        uri: Uri,
        projection: Array<String>,
        albumId: String,
        offset: Int,
        limit: Int
    ): Cursor? {
        val selection = baseSelectionClause + "AND ${MediaStore.MediaColumns.IS_PENDING} = ?" +
                " AND ${MediaStore.MediaColumns.BUCKET_ID} = ?"
        val selectionArgs = baseSelectionArgs.apply {
            add("0")
            add(albumId)
        }.toTypedArray()

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

        return context.contentResolver.query(
            uri,
            projection,
            selectionBundle,
            null
        )
    }
}