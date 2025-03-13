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
        val limit = 20
        var index = 0

        while (true) {
            getCursor(
                uri = uri,
                projection = projection,
                albumId = null,
                offset = index * limit,
                limit = limit
            )?.use {
                while (it.moveToNext()) {
                    val id = it.getColumnString(index = MediaStore.MediaColumns.BUCKET_ID)
                    val title = it.getColumnString(
                        index = MediaStore.MediaColumns.BUCKET_DISPLAY_NAME
                    )?.lowercase()

                    if (id == null || title == null) continue

                    if (map.contains(id to title)) {
                        map[id to title] = map[id to title]?.plus(1) ?: 1
                    } else
                        map[id to title] = 1
                }

                if (it.count < limit) {
                    return map.toList().map {
                        Album(
                            id = it.first.first,
                            name = it.first.second,
                            count = it.second
                        )
                    }.toMutableList().apply {
                        add(
                            0,
                            Album(
                                id = null,
                                name = "total",
                                count = sumOf { it.count }
                            )
                        )
                    }
                }
            } ?: break

            index++
        }

        return map.toList().map {
            Album(
                id = it.first.first,
                name = it.first.second,
                count = it.second
            )
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
}