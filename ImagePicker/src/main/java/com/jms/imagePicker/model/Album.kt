package com.jms.imagePicker.model

/**
 *
 * Album
 * @param id: album id (when id is null, total album)
 * @param name: album name
 * @param count: image count in album group
 */
data class Album(
    val id: String? = null,
    val name: String = "",
    val count: Int = 0,
)