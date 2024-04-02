package com.jms.galleryselector.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat

internal object Utils {
    fun createTempMediaFile(fileName: String, fileExtension: String): File {
        val dir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        val file = File(dir.absolutePath + "/Camera/$fileName$fileExtension")
        return file
    }

    fun saveImageToMediaStore(context: Context, file: File) {
        val currentTimeMillis = System.currentTimeMillis()
        val name = SimpleDateFormat("yyyyMMdd_HHmmss").format(currentTimeMillis)
        val bitmap = rotateBitmap(file = file)

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.DATE_TAKEN, currentTimeMillis)
            put(MediaStore.Images.Media.DATE_ADDED, currentTimeMillis / 1000) //sec
            put(MediaStore.Images.Media.DATE_MODIFIED, currentTimeMillis / 1000) //sec
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_DCIM + File.separator + "Camera"
            )
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
                throw e
            }
        } else {
            file.outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
            contentValues.put(MediaStore.Images.Media.DATA, file.absolutePath)
            context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
        }
    }

    private fun rotateBitmap(file: File): Bitmap {
        val orientation = getOrientation(file = file)
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)

        val matrix = Matrix()
        if (orientation != 0f) {
            matrix.postRotate(orientation)
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun getOrientation(file: File): Float {
        var orientation = ExifInterface.ORIENTATION_UNDEFINED

        try {
            val exif = ExifInterface(file.absolutePath)
            orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )

        } catch (e: Exception) {
            throw e
        }

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
    }
}