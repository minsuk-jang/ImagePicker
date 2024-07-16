package com.jms.galleryselector.manager

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.os.bundleOf

internal abstract class MediaContentManager(
    private val context: Context
) {
    protected val baseSelectionClause = "${MediaStore.Images.Media.MIME_TYPE} != ?"
    protected val baseSelectionArgs = arrayListOf("image/gif")

    fun getAlbumCursor(
        uri: Uri,
        total: Boolean = false,
    ): Cursor? {
        val projection = arrayOf(
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.BUCKET_ID,
            //"COUNT(${MediaStore.Images.Media._ID})"
        )

        val baseSelection = "${MediaStore.Images.Media.MIME_TYPE} != ?"
        val baseSelectionArgs = arrayListOf("image/gif")

        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            val selection = baseSelection +
                    "AND ${MediaStore.MediaColumns.IS_PENDING} = ? "
            val selectionArgs = baseSelectionArgs.apply {
                add("0")
            }.toTypedArray()

            val selectionBundle = bundleOf(
                ContentResolver.QUERY_ARG_SQL_SELECTION to selection,
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to selectionArgs,
                ContentResolver.QUERY_ARG_SORT_DIRECTION to ContentResolver.QUERY_SORT_DIRECTION_DESCENDING,
                ContentResolver.QUERY_ARG_SORT_COLUMNS to arrayOf(
                    MediaStore.Files.FileColumns.DATE_MODIFIED
                )
            ).apply {
                when (total) {
                    true -> Unit
                    false -> {
                        putString(
                            ContentResolver.QUERY_ARG_SQL_GROUP_BY,
                            MediaStore.Images.Media.BUCKET_ID
                        )
                    }
                }
            }

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
                baseSelection,
                baseSelectionArgs.toTypedArray(),
                "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
            )
        }
    }

    abstract fun getCursor(
        uri: Uri,
        projection: Array<String>,
        albumId: String,
        offset: Int,
        limit: Int,
    ): Cursor?
}