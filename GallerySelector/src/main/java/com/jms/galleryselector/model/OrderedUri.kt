package com.jms.galleryselector.model

import android.net.Uri

data class OrderedUri(
    val order: Int,
    val uri: Uri,
    val prev : Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OrderedUri) return false
        return uri == other.uri
    }

    override fun hashCode(): Int {
        return uri.hashCode()
    }
}