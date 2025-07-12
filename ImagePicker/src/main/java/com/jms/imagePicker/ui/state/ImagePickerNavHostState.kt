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
 * @param max: maximum selection count
 * @param autoSelectAfterCapture: auto select after capture
 */
@Composable
fun rememberImagePickerNavHostState(
    max: Int = Constants.MAX_SIZE,
    autoSelectAfterCapture: Boolean = false,
): ImagePickerNavHostState {
    return remember {
        ImagePickerNavHostState(
            max = max,
            autoSelectAfterCapture = autoSelectAfterCapture,
        )
    }
}

@Stable
class ImagePickerNavHostState(
    val max: Int = Constants.MAX_SIZE,
    val autoSelectAfterCapture: Boolean = false
) {
    private var _selectedMediaContents: MutableState<List<MediaContent>> =
        mutableStateOf(emptyList())

    //Currently selected media Content list
    val selectedMediaContents: List<MediaContent> get() = _selectedMediaContents.value

    internal fun updateMediaContents(mediaContents: List<MediaContent>) {
        _selectedMediaContents.value = mediaContents
    }
}