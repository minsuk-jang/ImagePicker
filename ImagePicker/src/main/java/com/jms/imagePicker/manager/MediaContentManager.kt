package com.jms.imagePicker.manager

import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.jms.imagePicker.extensions.getColumnString
import com.jms.imagePicker.model.Album

internal abstract class MediaContentManager {
    protected val baseSelectionClause = "${MediaStore.Images.Media.MIME_TYPE} != ?"
    protected val baseSelectionArgs = arrayListOf("image/gif")

    fun getAlbums(uri: Uri): List<Album> {
        val projection = arrayOf(
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.BUCKET_ID
        )
        val map = mutableMapOf<Pair<String, String>, Int>()

        getAlbumCursor(uri = uri, projection = projection)?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getColumnString(index = MediaStore.MediaColumns.BUCKET_ID)
                val title = cursor.getColumnString(
                    index = MediaStore.MediaColumns.BUCKET_DISPLAY_NAME
                )?.lowercase()

                if (id == null || title == null) continue

                map[id to title] = (map[id to title] ?: 0) + 1
            }
        }

        return buildAlbumList(map)
    }

    private fun buildAlbumList(map: Map<Pair<String, String>, Int>): List<Album> {
        return map.toList().map {
            Album(id = it.first.first, name = it.first.second, count = it.second)
        }.toMutableList().apply {
            add(0, Album(id = null, name = "total", count = sumOf { it.count }))
        }
    }

    abstract fun getCursor(
        uri: Uri,
        projection: Array<String>,
        albumId: String?,
        offset: Int,
        limit: Int,
    ): Cursor?

    abstract fun getAlbumCursor(uri: Uri, projection: Array<String>): Cursor?

    abstract fun getCursorByIds(uri: Uri, projection: Array<String>, ids: List<Long>): Cursor?
}