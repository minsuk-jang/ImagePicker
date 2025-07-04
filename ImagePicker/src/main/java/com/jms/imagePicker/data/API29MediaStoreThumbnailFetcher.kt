package com.jms.imagePicker.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toDrawable
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import com.jms.imagePicker.extensions.toAndroidSize
import android.util.Size as AndroidSize

@RequiresApi(Build.VERSION_CODES.Q)
internal class API29MediaStoreThumbnailFetcher private constructor(
    private val context: Context,
    private val uri: Uri,
    private val pixelSize: AndroidSize,
) : Fetcher {
    override suspend fun fetch(): FetchResult? {
        val bitmap = context.contentResolver.loadThumbnail(uri, pixelSize, null)

        return DrawableResult(
            drawable = bitmap.toDrawable(context.resources),
            isSampled = true,
            dataSource = DataSource.DISK
        )
    }

    class Factory(
        private val context: Context
    ) : Fetcher.Factory<Uri> {
        override fun create(
            data: Uri,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher? {
            if (data.scheme != ContentResolver.SCHEME_CONTENT) return null

            return API29MediaStoreThumbnailFetcher(
                context = context,
                uri = data,
                pixelSize = options.size.toAndroidSize()
            )
        }
    }
}
