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


@SuppressLint("ModifierFactoryUnreferencedReceiver")
internal fun Modifier.photoGridDragHandler(
    lazyGridState: LazyGridState,
    selectedImages: List<Uri>,
    haptics: HapticFeedback,
    autoScrollSpeed: MutableState<Float>,
    autoScrollThreshold: Float,
    onSelect: (Uri) -> Unit = {},
    onGroupSelect: (start: Int?, middle: Int?, end: Int?, isInstantForward: Boolean, isForward: Boolean) -> Unit = { _, _, _, _, _ -> }
) = pointerInput(Unit) {
    var initialKey: Uri? = null
    var initialIndex: Int? = null
    var currentKey: Uri? = null
    var currentIndex: Int? = null

    var startRow = -1
    var prevRow = -1

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
                    startRow = info.row
                    prevRow = info.row

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
            startRow = -1
            prevRow = -1

            autoScrollSpeed.value = 0f
        },
        onDrag = { change, _ ->
            if (initialKey != null) {
                val distFromBottom =
                    lazyGridState.layoutInfo.viewportSize.height - change.position.y
                val distFromTop = change.position.y

                autoScrollSpeed.value = when {
                    distFromBottom < autoScrollThreshold -> 9.5f
                    distFromTop < autoScrollThreshold -> -9.5f
                    else -> 0f
                }

                lazyGridState.gridItemKeyAtPosition(change.position)?.let { info ->
                    val key = info.key as? Uri
                    val index = info.index

                    if (key == null) return@detectDragGesturesAfterLongPress

                    if (currentKey != key) {
                        val deltaY = info.row - prevRow
                        val isForward = when (info.row == startRow) {
                            true -> !(deltaY > 0)
                            false -> info.row > startRow
                        }
                        val isInstantForward = deltaY > 0

                        Log.e(
                            TAG, "[Drag]\n" +
                                    "isForward: $isForward || isInstantForward: $isInstantForward\n" +
                                    "prev row: ${prevRow} || row: ${info.row}\n" +
                                    "delta: $deltaY"
                        )

                        onGroupSelect(
                            initialIndex,
                            currentIndex,
                            index,
                            isInstantForward,
                            isForward
                        )

                        currentKey = key
                        currentIndex = index
                        prevRow = info.row
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