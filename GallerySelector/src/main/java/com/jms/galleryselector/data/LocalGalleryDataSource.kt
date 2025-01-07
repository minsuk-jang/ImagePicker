package com.jms.galleryselector.data

import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.jms.galleryselector.Constants
import com.jms.galleryselector.extensions.getColumnString
import com.jms.galleryselector.manager.MediaContentManager
import com.jms.galleryselector.model.Album
import com.jms.galleryselector.model.Gallery
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
        return contentManager.getAlbums(uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    }

    fun getLocalGalleryImages(
        page: Int = 1,
        albumId: String?,
        pageSize: Int = Constants.DEFAULT_PAGE_SiZE
    ): Flow<PagingData<Gallery.Image>> {
        return galleryStream.load {
            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            val _page = it.key ?: page

            contentManager.getCursor(
                uri = uri,
                offset = (_page - 1) * pageSize,
                albumId = albumId,
                limit = pageSize,
                projection = arrayOf(
                    MediaStore.MediaColumns._ID,
                    MediaStore.MediaColumns.TITLE,
                    MediaStore.MediaColumns.DATE_MODIFIED,
                    MediaStore.MediaColumns.DATA,
                    MediaStore.MediaColumns.MIME_TYPE,
                    MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
                    MediaStore.MediaColumns.BUCKET_ID
                )
            )?.use { cursor ->
                val list = buildList {
                    while (cursor.moveToNext()) {
                        add(toImage(cursor = cursor))
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

    fun getLocalGalleryImage(uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI): Gallery.Image {
        contentManager.getCursor(
            uri = uri,
            offset = 0,
            albumId = null,
            limit = 1,
            projection = arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.TITLE,
                MediaStore.MediaColumns.DATE_MODIFIED,
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
                MediaStore.MediaColumns.BUCKET_ID
            )
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                return toImage(cursor = cursor)
            } else throw IllegalStateException("Cursor is empty!!")
        } ?: throw IllegalStateException("Cursor is null!!")
    }

    private fun toImage(cursor: Cursor): Gallery.Image {
        val id =
            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
        val title =
            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.TITLE))
        val dateAt =
            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED))

        val mimeType =
            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE))

        val uri = Uri.withAppendedPath(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            id.toString()
        )

        val data =
            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))

        val album =
            cursor.getColumnString(index = MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
        val albumId = cursor.getColumnString(index = MediaStore.MediaColumns.BUCKET_ID)

        return Gallery.Image(
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