package com.jms.galleryselector.extensions

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toIntRect
import com.jms.galleryselector.Constants.TAG
import kotlin.math.log


@SuppressLint("ModifierFactoryUnreferencedReceiver")
internal fun Modifier.photoGridDragHandler(
    lazyGridState: LazyGridState,
    selectedImages: List<Uri>,
    haptics: HapticFeedback,
    autoScrollSpeed: MutableState<Float>,
    autoScrollThreshold: Float,
    onSelect: (Uri) -> Unit = {},
    onGroupSelect: (start: Int?, end: Int?) -> Unit = { _, _ -> }
) = pointerInput(Unit) {
    var initialKey: Uri? = null
    var initialIndex: Int? = null
    var currentKey: Uri? = null
    var currentIndex: Int? = null

    detectDragGesturesAfterLongPress(
        onDragStart = { offset ->
            lazyGridState.gridItemKeyAtPosition(offset)?.let { info ->
                val key = info.key as? Uri
                val index = info.index

                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                if (key == null) return@detectDragGesturesAfterLongPress

                if (!selectedImages.contains(key)) {
                    initialKey = key
                    initialIndex = index
                    currentKey = key
                    currentIndex = index

                    onSelect(key)
                }
            }
        },
        onDragCancel = {
            initialKey = null
            initialIndex = null

            autoScrollSpeed.value = 0f
        },
        onDragEnd = {
            initialKey = null
            initialIndex = null

            autoScrollSpeed.value = 0f
        },
        onDrag = { change, _ ->
            if (initialKey != null) {
                val distFromBottom =
                    lazyGridState.layoutInfo.viewportSize.height - change.position.y
                val distFromTop = change.position.y

                autoScrollSpeed.value = when {
                    distFromBottom < autoScrollThreshold -> autoScrollThreshold - distFromBottom
                    distFromTop < autoScrollThreshold -> -(autoScrollThreshold - distFromTop)
                    else -> 0f
                }

                lazyGridState.gridItemKeyAtPosition(change.position)?.let { info ->
                    val key = info.key as? Uri
                    val index = info.index

                    if (key == null) return@detectDragGesturesAfterLongPress

                    if (currentKey != key) {
                        onGroupSelect(initialIndex, index)
                        currentKey = key
                    }
                }
            }
        }
    )
}

internal fun LazyGridState.gridItemKeyAtPosition(hitPoint: Offset): LazyGridItemInfo? {
    return layoutInfo.visibleItemsInfo.find { itemInfo ->
        itemInfo.size.toIntRect().contains(hitPoint.round() - itemInfo.offset)
    }
}