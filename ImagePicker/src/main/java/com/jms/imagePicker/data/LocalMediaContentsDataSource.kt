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

    fun getMediaContent(uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI): MediaContent {
        val cursor = contentManager.getCursor(
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
        )

        if (cursor == null)
            throw IllegalStateException("Cursor is null!!")

        return if (cursor.moveToFirst()) {
            cursor.toImage()
        } else
            throw IllegalStateException("Cursor is null!!")
    }
}