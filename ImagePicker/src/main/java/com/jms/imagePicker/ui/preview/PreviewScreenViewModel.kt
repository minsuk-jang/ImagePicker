package com.jms.imagePicker.ui.preview

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.jms.imagePicker.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


internal data class PreviewScreenUiModel(
    val uri: Uri = Uri.EMPTY,
    val originSelected: Boolean = false,
    val selected: Boolean = false,
    val order: Int = Constants.NO_ORDER,
)

internal class PreviewScreenViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiModel: MutableStateFlow<PreviewScreenUiModel> =
        MutableStateFlow(PreviewScreenUiModel())
    val uiModel: StateFlow<PreviewScreenUiModel> = _uiModel.asStateFlow()


    init {
        getPreviewImage()
    }

    private fun getPreviewImage() {
        val uri = savedStateHandle.get<Uri>("uri") ?: Uri.EMPTY
        val originSelected = savedStateHandle.get<Boolean>("selected") ?: false
        val order =
            savedStateHandle.get<Int>("order")?.coerceAtLeast(0)?.plus(1) ?: Constants.NO_ORDER

        _uiModel.update {
            it.copy(
                uri = uri,
                originSelected = originSelected,
                selected = originSelected,
                order = order
            )
        }
    }

    fun select() {
        _uiModel.update {
            it.copy(
                selected = !it.selected
            )
        }
    }
}