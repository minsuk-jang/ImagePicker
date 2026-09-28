package com.jms.imagePicker.data

import android.provider.MediaStore
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.jms.imagePicker.extensions.toImage
import com.jms.imagePicker.manager.MediaContentManager
import com.jms.imagePicker.model.MediaContent
import com.jms.imagePicker.model.MediaSeekKey


internal class ImagePickerPagingDataSource(
    private val contentManager: MediaContentManager,
    private val albumId: String?
) : PagingSource<MediaSeekKey, MediaContent>() {

    companion object {
        const val DEFAULT_PAGE_LIMIT = 30
    }

    private val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    private val observer = contentManager.registerObserver(uri = uri) { invalidate() }

    init {
        registerInvalidatedCallback { contentManager.unregisterObserver(observer) }
    }

    override fun getRefreshKey(state: PagingState<MediaSeekKey, MediaContent>): MediaSeekKey? = null

    override suspend fun load(params: LoadParams<MediaSeekKey>): LoadResult<MediaSeekKey, MediaContent> {
        return try {
            val limit = params.loadSize

            contentManager.getCursor(
                uri = uri,
                seekKey = params.key,
                albumId = albumId,
                limit = limit,
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
                    while (cursor.moveToNext()) add(cursor.toImage())
                }

                val nextKey = if (list.size == limit) {
                    list.lastOrNull()?.let { MediaSeekKey(dateModified = it.dateAt, id = it.id) }
                } else null

                LoadResult.Page(
                    data = list,
                    prevKey = null,
                    nextKey = nextKey
                )
            } ?: LoadResult.Error(throwable = Exception("Empty Gallery"))
        } catch (e: Exception) {
            LoadResult.Error(throwable = e)
        }
    }
}
