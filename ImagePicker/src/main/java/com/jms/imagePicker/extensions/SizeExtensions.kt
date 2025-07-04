package com.jms.imagePicker.extensions

import coil.size.Size
import coil.size.pxOrElse
import android.util.Size as AndroidSize

internal fun Size.toAndroidSize(): AndroidSize {
    return AndroidSize(
        width.pxOrElse { 512 },
        height.pxOrElse { 512 }
    )
}