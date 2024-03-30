package com.jms.galleryselector.data

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.os.bundleOf
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.jms.galleryselector.Constants
import com.jms.galleryselector.model.ImageEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat

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

                        add(
                            ImageEntity(
                                id = id,
                                title = title,
                                dateAt = dateAt,
                                data = data,
                                uri = uri,
                                mimeType = mimeType,
                                album = album
                            )
                        )
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


    private fun getCursor(uri: Uri, offset: Int, limit: Int): Cursor? {
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            val selectionBundle = bundleOf(
                ContentResolver.QUERY_ARG_OFFSET to offset,
                ContentResolver.QUERY_ARG_LIMIT to limit,
                ContentResolver.QUERY_ARG_SORT_COLUMNS to arrayOf(MediaStore.Files.FileColumns.DATE_MODIFIED),
                ContentResolver.QUERY_ARG_SORT_DIRECTION to
                        ContentResolver.QUERY_SORT_DIRECTION_DESCENDING,
                ContentResolver.QUERY_ARG_SQL_SELECTION to null,
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to null
            )

            context.contentResolver.query(
                uri,
                getGalleryImageProjections(),
                selectionBundle,
                null
            )
        } else {
            context.contentResolver.query(
                uri,
                getGalleryImageProjections(),
                null,
                null,
                "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC LIMIT $limit OFFSET $offset"
            )
        }
    }

    private fun getGalleryImageProjections(): Array<String> {
        return arrayOf(
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.TITLE,
            MediaStore.Images.ImageColumns.DATE_MODIFIED,
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.MIME_TYPE,
            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME
        )
    }
}