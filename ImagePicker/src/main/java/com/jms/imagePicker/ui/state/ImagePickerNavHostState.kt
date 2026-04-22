package com.jms.imagePicker.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.jms.imagePicker.Constants
import com.jms.imagePicker.model.MediaContent

/**
 *
 * Remembers and provides the [ImagePickerNavHostState] to be used with [ImagePickerNavHost].
 *
 * @param max The maximum number of media items allowed to be selected.
 *
 */
@Composable
fun rememberImagePickerNavHostState(
    max: Int = Constants.MAX_SIZE,
): ImagePickerNavHostState {
    return remember {
        ImagePickerNavHostState(max = max)
    }
}

/**
 *
 * Holds the selection state and configuration for the [ImagePickerNavHost].
 *
 * @param max The maximum number of media items allowed to be selected.
 *
 */
@Stable
class ImagePickerNavHostState(
    val max: Int = Constants.MAX_SIZE,
) {
    private var _selectedMediaContents: MutableState<List<MediaContent>> =
        mutableStateOf(emptyList())

    /**
     *
     * The currently selected list of media content.
     */
    val selectedMediaContents: List<MediaContent> get() = _selectedMediaContents.value

    internal fun updateMediaContents(mediaContents: List<MediaContent>) {
        _selectedMediaContents.value = mediaContents
    }
}