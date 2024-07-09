package com.jms.galleryselector.data

import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.jms.galleryselector.Constants
import com.jms.galleryselector.manager.MediaContentManager
import com.jms.galleryselector.model.Album
import com.jms.galleryselector.model.ImageEntity
import kotlinx.coroutines.flow.Flow

/**
 *
 * Local Gallery data source
 */
internal class LocalGalleryDataSource(
    private val contentManager: MediaContentManager,
    private val galleryStream: GalleryPagingStream
) {
    fun getAlbums(): List<Album> {
        return buildList {
            //total
            contentManager.getAlbumCursor(
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                total = true
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val albumName =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME))
                    val albumId =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID))
                    val count = cursor.getInt(2)

                    add(
                        Album(
                            id = albumId,
                            name = albumName,
                            count = count
                        )
                    )
                }
            }

            //album grouping
            contentManager.getAlbumCursor(
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val albumName =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME))
                    val albumId =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID))
                    val count = cursor.getInt(2)

                    add(
                        Album(
                            id = albumId,
                            name = albumName,
                            count = count
                        )
                    )
                }
            }
        }
    }

    fun getLocalGalleryImages(
        page: Int = 1,
        albumId: String,
        pageSize: Int = Constants.DEFAULT_PAGE_SiZE
    ): Flow<PagingData<ImageEntity>> {
        return galleryStream.load {
            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            val _page = it.key ?: page

            contentManager.getCursor(
                uri = uri,
                offset = (_page - 1) * pageSize,
                albumId = albumId,
                limit = pageSize
            )?.use { cursor ->
                val list = buildList {
                    while (cursor.moveToNext()) {
                        add(makeImageEntity(cursor = cursor))
                    }
                }

                return@load PagingSource.LoadResult.Page(
                    data = list,
                    prevKey = null,
                    nextKey = if (list.isNotEmpty()) _page + 1 else null
                )
            } ?: PagingSource.LoadResult.Error(throwable = Exception("Empty Gallery"))
        }
    }

    fun getLocalGalleryImage(): ImageEntity {
        contentManager.getCursor(
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            offset = 0,
            albumId = "",
            limit = 1,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                return makeImageEntity(cursor = cursor)
            } else throw IllegalStateException("Cursor is empty!!")
        } ?: throw IllegalStateException("Cursor is null!!")
    }

    private fun makeImageEntity(cursor: Cursor): ImageEntity {
        val id =
            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
        val title =
            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE))
        val dateAt =
            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED))

        val mimeType =
            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE))

        val uri = Uri.withAppendedPath(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            id.toString()
        )

        val data =
            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))

        val album =
            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME))
        val albumId =
            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_ID))

        return ImageEntity(
            id = id,
            title = title,
            dateAt = dateAt,
            data = data,
            uri = uri,
            mimeType = mimeType,
            album = album,
            albumId = albumId
        )
    }
}