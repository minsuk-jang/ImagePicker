package com.jms.galleryselector.data

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.os.bundleOf
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.jms.galleryselector.Constants
import com.jms.galleryselector.model.ImageEntity
import kotlinx.coroutines.flow.Flow

/**
 *
 * Local Gallery data source
 */
internal class LocalGalleryDataSource(
    private val context: Context,
    private val galleryStream: GalleryPagingStream
) {
    fun getLocalGalleryImages(
        page: Int = 1,
        pageSize: Int = Constants.DEFAULT_PAGE_SiZE
    ): Flow<PagingData<ImageEntity>> {
        return galleryStream.load {
            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            val _page = it.key ?: page

            getCursor(
                uri = uri,
                offset = (_page - 1) * pageSize,
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

    fun getImageEntity(): ImageEntity {
        getCursor(
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            offset = 0,
            limit = 1,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                return makeImageEntity(cursor = cursor)
            } else throw IllegalStateException("Cursor is empty!!")
        } ?: throw IllegalStateException("Cursor is null!!")
    }

    private fun getCursor(
        uri: Uri,
        offset: Int,
        limit: Int,
    ): Cursor? {
        //filter deleted media contents
        val selection = MediaStore.MediaColumns.IS_PENDING + " = ?"
        val selectionArgs = arrayOf("0")
        val projection = arrayOf(
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.TITLE,
            MediaStore.Images.ImageColumns.DATE_MODIFIED,
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.MIME_TYPE,
            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Images.ImageColumns.BUCKET_ID
        )

        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
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
                selection,
                selectionArgs,
                "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC LIMIT $limit OFFSET $offset"
            )
        }
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