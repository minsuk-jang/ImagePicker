package com.jms.imagePicker.data

import android.provider.MediaStore
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.jms.imagePicker.Constants
import com.jms.imagePicker.extensions.toImage
import com.jms.imagePicker.manager.MediaContentManager
import com.jms.imagePicker.model.Gallery
import kotlinx.coroutines.flow.Flow


internal class ImagePickerPagingSource(
    private val contentManager: MediaContentManager,
    private val albumId: String?
) : PagingSource<Int, Gallery.Image>() {

    companion object {
        const val DEFAULT_PAGE = 1
        const val DEFAULT_PAGE_LIMIT = 30
    }

    override fun getRefreshKey(state: PagingState<Int, Gallery.Image>): Int? {
        return state.anchorPosition?.let { anchorPos ->
            val anchorPage = state.closestPageToPosition(anchorPos)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Gallery.Image> {
        return try {
            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            val page = params.key ?: DEFAULT_PAGE

            contentManager.getCursor(
                uri = uri,
                offset = (page - 1) * DEFAULT_PAGE_LIMIT,
                albumId = albumId,
                limit = DEFAULT_PAGE_LIMIT,
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
                        add(cursor.toImage())
                    }
                }

                return@load LoadResult.Page(
                    data = list,
                    prevKey = if (list.isNotEmpty()) page - 1 else null,
                    nextKey = if (list.isNotEmpty()) page + 1 else null
                )
            } ?: LoadResult.Error(throwable = Exception("Empty Gallery"))
        } catch (e: Exception) {
            LoadResult.Error(throwable = e)
        }
    }
}

