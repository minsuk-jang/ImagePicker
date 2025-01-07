package com.jms.galleryselector.model

import android.net.Uri

data class OrderedUri(
    val order: Int,
    val uri: Uri
)