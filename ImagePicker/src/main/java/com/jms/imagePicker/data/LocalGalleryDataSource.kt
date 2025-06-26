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
internal class LocalGalleryDataSource(
    private val contentManager: MediaContentManager
) {
    fun getAlbums(): List<Album> {
        return contentManager.getAlbums(uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    }

    fun getLocalGalleryImages(
        albumId: String?
    ): Flow<PagingData<MediaContent>> {
        return Pager(
            config = PagingConfig(
                pageSize = ImagePickerPagingDataSource.DEFAULT_PAGE_LIMIT
            )
        ) {
            ImagePickerPagingDataSource(
                contentManager = contentManager,
                albumId = albumId
            )
        }.flow
    }

    fun getLocalGalleryImage(uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI): MediaContent {
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
                return cursor.toImage()
            } else throw IllegalStateException("Cursor is empty!!")
        } ?: throw IllegalStateException("Cursor is null!!")
    }
}