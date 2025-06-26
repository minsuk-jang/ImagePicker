package com.jms.imagePicker.extensions

import android.annotation.SuppressLint
import android.net.Uri
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
import com.jms.imagePicker.model.Gallery


@SuppressLint("ModifierFactoryUnreferencedReceiver")
internal fun Modifier.photoGridDragHandler(
    lazyGridState: LazyGridState,
    selectedUris: List<Uri>,
    haptics: HapticFeedback,
    autoScrollSpeed: MutableState<Float>,
    autoScrollThreshold: Float,
    onDragStart: (Uri) -> Unit = {},
    onDrag: (start: Int?, end: Int?) -> Unit = { _, _ -> },
    onDragEnd: () -> Unit = {}
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

                if (!selectedUris.contains(key)) {
                    initialKey = key
                    initialIndex = index
                    currentKey = key
                    currentIndex = index

                    onDragStart(key)
                }
            }
        },
        onDragCancel = {
            initialKey = null
            initialIndex = null

            autoScrollSpeed.value = 0f

            onDragEnd()
        },
        onDragEnd = {
            initialKey = null
            initialIndex = null

            autoScrollSpeed.value = 0f

            onDragEnd()
        },
        onDrag = { change, _ ->
            if (initialKey != null) {
                val distFromBottom =
                    lazyGridState.layoutInfo.viewportSize.height - change.position.y
                val distFromTop = change.position.y

                autoScrollSpeed.value = when {
                    distFromBottom < autoScrollThreshold -> 15f
                    distFromTop < autoScrollThreshold -> -15f
                    else -> 0f
                }

                lazyGridState.gridItemKeyAtPosition(change.position)?.let { info ->
                    val key = info.key as? Uri
                    val index = info.index

                    if (key == null) return@detectDragGesturesAfterLongPress

                    if (currentKey != key) {
                        onDrag(
                            initialIndex,
                            index
                        )

                        currentKey = key
                        currentIndex = index
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