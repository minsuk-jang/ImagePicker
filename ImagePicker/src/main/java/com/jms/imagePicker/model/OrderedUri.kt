package com.jms.imagePicker.model

import android.net.Uri
import androidx.compose.runtime.Stable

@Stable
internal data class OrderedUri(
    val order: Int,
    val uri: Uri
)