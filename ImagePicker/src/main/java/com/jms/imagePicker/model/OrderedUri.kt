package com.jms.imagePicker.model

import android.net.Uri

data class OrderedUri(
    val order: Int,
    val uri: Uri
)