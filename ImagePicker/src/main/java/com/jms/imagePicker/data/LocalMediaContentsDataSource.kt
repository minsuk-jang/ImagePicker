package com.jms.imagePicker.data

import android.net.Uri
import android.provider.MediaStore
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.jms.imagePicker.extensions.toImage
import com.jms.imagePicker.manager.MediaContentManager
import com.jms.imagePicker.model.Album
import com.jms.imagePicker.model.MediaContent
import kotlinx.coroutines.flow.Flow

/**
 *
 * Local Gallery data source
 */
internal class LocalMediaContentsDataSource(
    private val contentManager: MediaContentManager
) {
    fun getAlbums(): List<Album> {
        return contentManager.getAlbums(uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    }

    fun getMediaContents(
        albumId: String?
    ): Flow<PagingData<MediaContent>> {
        return Pager(
            config = PagingConfig(
                pageSize = ImagePickerPagingDataSource.DEFAULT_PAGE_LIMIT,
                initialLoadSize = 20,
            )
        ) {
            ImagePickerPagingDataSource(
                contentManager = contentManager,
                albumId = albumId
            )
        }.flow
    }

    fun getMediaContents(uris: List<Uri>): List<MediaContent> {
        if (uris.isEmpty()) return emptyList()

        val ids = uris.mapNotNull { it.lastPathSegment?.toLongOrNull() }
        if (ids.isEmpty()) return emptyList()

        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.TITLE,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.BUCKET_ID
        )

        val mediaMap = mutableMapOf<Uri, MediaContent>()
        contentManager.getCursorByIds(
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection = projection,
            ids = ids
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val item = cursor.toImage()
                mediaMap[item.uri] = item
            }
        }

        return uris.mapNotNull { mediaMap[it] }
    }
}