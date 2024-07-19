package com.jms.galleryselector.manager

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.exifinterface.media.ExifInterface
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

        val dirPath = context.filesDir.path + "/" + "image"

        if (!File(dirPath).exists()) {
            File(dirPath).mkdirs()
        }
        Log.e("jms8732", "createImageFile: $dirPath")

        //Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        return File("$dirPath/$name.jpg")
    }

    fun saveImageFile(context: Context, file: File) {
        val currentTimeMillis = file.lastModified()
        val bitmap = rotateBitmap(file = file)

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, file.nameWithoutExtension)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.DATE_TAKEN, currentTimeMillis)
            put(MediaStore.Images.Media.DATE_ADDED, currentTimeMillis / 1000) //sec
            put(MediaStore.Images.Media.DATE_MODIFIED, currentTimeMillis / 1000) //sec
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val imageUri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues.apply {
                        put(
                            MediaStore.Images.Media.RELATIVE_PATH,
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

    fun deleteImageFile(file: File) {
        file.delete()
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