package com.jms.imagePicker.ui.preview

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


internal data class PreviewScreenUiModel(
    val uri: Uri = Uri.EMPTY
)

internal class PreviewScreenViewModel(
    val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiModel: MutableStateFlow<PreviewScreenUiModel> =
        MutableStateFlow(PreviewScreenUiModel())
    val uiModel: StateFlow<PreviewScreenUiModel> = _uiModel.asStateFlow()


    init {
        getPreviewImage()
    }

    private fun getPreviewImage() {
        val uri = savedStateHandle.get<String>("uri")

        Log.e("jms8732", "getPreviewImage: $uri", )
        _uiModel.update {
            it.copy(
                Uri.parse(uri)
            )
        }
    }
}