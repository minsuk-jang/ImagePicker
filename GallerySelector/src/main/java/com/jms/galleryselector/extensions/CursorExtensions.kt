package com.jms.galleryselector.extensions

import android.database.Cursor


/**
 *
 * get column string
 */
fun Cursor.getColumnString(index: String): String? {
    val columnIndex = getColumnIndex(index)
    return if (columnIndex != -1)
        getString(columnIndex)
    else null
}