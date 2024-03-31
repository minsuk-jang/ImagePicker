package com.jms.galleryselector.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat

internal object Utils {
    fun createTempMediaFile(fileName: String, fileExtension: String): File {
        val dir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, fileExtension, dir)
    }

    fun saveImageToMediaStore(context: Context, uri: Uri) {
        val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))

        val currentTimeMillis = System.currentTimeMillis()
        val name = SimpleDateFormat("yyyyMMdd_HHmmss").format(currentTimeMillis)

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, name)
            put(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME, "Camera")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
            put(MediaStore.Images.Media.DATE_TAKEN, currentTimeMillis)
            put(MediaStore.Images.Media.DATE_ADDED, currentTimeMillis / 1000) //sec
            put(MediaStore.Images.Media.DATE_MODIFIED, currentTimeMillis / 1000) //sec
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val imageUri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ) ?: throw IOException("Failed to create new MediaStore record.")

                context.contentResolver.openOutputStream(imageUri)?.use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                } ?: throw IOException("Failed to open output stream.")

            } catch (e: Exception) {

            }
        } else {
            val dir =
                Environment.getExternalStorageDirectory().toString()
            val image = File(dir, "$name.jpg")
            val fos = FileOutputStream(image)

            fos.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
            contentValues.put(MediaStore.Images.Media.DATA, image.absolutePath)
            context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
        }
    }
}