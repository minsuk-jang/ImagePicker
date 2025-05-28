package com.jms.imagePicker.manager

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat


internal class FileManager(
    private val context: Context
) {
    companion object {
        const val PATTERN = "yyyyMMdd_HHmmss"
    }

    @SuppressLint("SimpleDateFormat")
    fun createImageFile(): File {
        val name = SimpleDateFormat(PATTERN).format(System.currentTimeMillis())

        val dir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/Camera")

        if (!dir.exists())
            dir.mkdirs()

        return File(dir.absolutePath + "/$name.jpg")
    }

    suspend fun saveImageFile(context: Context, file: File) = withContext(Dispatchers.IO) {
        val currentTimeMillis = file.lastModified()
        val bitmap = rotateBitmap(file = file)

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.TITLE, file.nameWithoutExtension)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.DATE_TAKEN, currentTimeMillis)
            put(MediaStore.MediaColumns.DATE_ADDED, currentTimeMillis / 1000) //sec
            put(MediaStore.MediaColumns.DATE_MODIFIED, currentTimeMillis / 1000) //sec
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val imageUri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues.apply {
                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            Environment.DIRECTORY_DCIM + File.separator + "Camera"
                        )
                    }
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
            context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues.apply {
                    put(MediaStore.Images.Media.DATA, file.absolutePath)
                }
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
        return kotlin.runCatching {
            val exif = ExifInterface(file.absolutePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        }.getOrElse {
            throw it
        }
    }
}