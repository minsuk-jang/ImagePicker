package com.jms.imagePicker.data

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.graphics.drawable.toDrawable
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import com.jms.imagePicker.extensions.toAndroidSize
import android.util.Size as AndroidSize

class API21MediaStoreThumbnailFetcher private constructor(
    private val context: Context,
    private val uri: Uri,
    private val pixelSize: AndroidSize
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        tryGetMediaStoreThumbnail(uri = uri)?.let { thumbnail ->
            return DrawableResult(
                drawable = thumbnail.toDrawable(context.resources),
                isSampled = true,
                dataSource = DataSource.DISK
            )
        }

        context.contentResolver.openInputStream(uri)?.use {
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(it, null, opts)

            opts.inSampleSize = calculateSampleSize(
                sourceWidth = opts.outWidth,
                sourceHeight = opts.outHeight,
                targetWidth = pixelSize.width,
                targetHeight = pixelSize.height
            )
            opts.inJustDecodeBounds = false

            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, opts)?.let {
                    return DrawableResult(
                        drawable = it.toDrawable(context.resources),
                        isSampled = true,
                        dataSource = DataSource.DISK
                    )
                }
            }
        }

        throw IllegalStateException("Legacy thumbnail fetch failed")
    }

    private fun tryGetMediaStoreThumbnail(uri: Uri): Bitmap? {
        val id = runCatching { ContentUris.parseId(uri) }.getOrNull() ?: return null

        return MediaStore.Images.Thumbnails.getThumbnail(
            context.contentResolver,
            id,
            MediaStore.Images.Thumbnails.MINI_KIND,
            null
        )
    }

    private fun calculateSampleSize(
        sourceWidth: Int,
        sourceHeight: Int,
        targetWidth: Int,
        targetHeight: Int
    ): Int {
        if (targetWidth <= 0 || targetHeight <= 0) return 1

        var sample = 1
        while (sourceWidth / (sample * 2) >= targetWidth && sourceHeight / (sample * 2) >= targetHeight) {
            sample *= 2
        }

        return sample
    }

    class Factory(private val context: Context) : Fetcher.Factory<Uri> {
        override fun create(
            data: Uri,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher? {
            if (data.scheme != ContentResolver.SCHEME_CONTENT) return null

            return API21MediaStoreThumbnailFetcher(
                context = context,
                uri = data,
                pixelSize = options.size.toAndroidSize()
            )
        }
    }
}