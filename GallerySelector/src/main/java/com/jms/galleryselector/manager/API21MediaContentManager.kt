package com.jms.galleryselector.manager

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
        return context.contentResolver.query(
            uri,
            projection,
            baseSelectionClause,
            baseSelectionArgs.toTypedArray(),
            "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC LIMIT $limit OFFSET $offset"
        )
    }
}