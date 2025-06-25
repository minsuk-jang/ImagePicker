package com.jms.imagePicker.ui.preview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.jms.imagePicker.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


internal data class PreviewScreenUiModel(
    val uri: String = "",
    val originSelected: Boolean = false,
    val selected: Boolean = false,
    val order: Int = Constants.NO_ORDER,
){
    val isChanged get() = originSelected != selected
}

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
        val uri = savedStateHandle.get<String>(Constants.KEY_URI) ?: ""
        val originSelected = savedStateHandle.get<Boolean>(Constants.KEY_SELECTED) ?: false
        val order =
            savedStateHandle.get<Int>(Constants.KEY_ORDER) ?: Constants.NO_ORDER

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