package com.jms.galleryselector.data

import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.jms.galleryselector.Constants
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
                    MediaStore.Images.ImageColumns._ID,
                    MediaStore.Images.ImageColumns.TITLE,
                    MediaStore.Images.ImageColumns.DATE_MODIFIED,
                    MediaStore.Images.ImageColumns.DATA,
                    MediaStore.Images.ImageColumns.MIME_TYPE,
                    MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.BUCKET_ID
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

    fun getLocalGalleryImage(): Gallery.Image {
        contentManager.getCursor(
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            offset = 0,
            albumId = null,
            limit = 1,
            projection = arrayOf(
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.TITLE,
                MediaStore.Images.ImageColumns.DATE_MODIFIED,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.MIME_TYPE,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.BUCKET_ID
            )
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                return toImage(cursor = cursor)
            } else throw IllegalStateException("Cursor is empty!!")
        } ?: throw IllegalStateException("Cursor is null!!")
    }

    private fun toImage(cursor: Cursor): Gallery.Image {
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