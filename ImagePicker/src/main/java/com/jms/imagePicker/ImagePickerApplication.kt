package com.jms.imagePicker

import android.app.Application
import android.os.Build
import coil.Coil
import coil.ImageLoader
import com.jms.imagePicker.data.API21MediaStoreThumbnailFetcher
import com.jms.imagePicker.data.API29MediaStoreThumbnailFetcher

internal class ImagePickerApplication : Application() {
    val imageLoader: ImageLoader by lazy {
        ImageLoader.Builder(this)
            .components {
                if (Build.VERSION_CODES.Q <= Build.VERSION.SDK_INT)
                    add(API29MediaStoreThumbnailFetcher.Factory(context = this@ImagePickerApplication))
                else
                    add(API21MediaStoreThumbnailFetcher.Factory(context = this@ImagePickerApplication))
            }
            .build()
    }

    override fun onCreate() {
        super.onCreate()
    }
}