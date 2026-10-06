package com.jms.imagePicker.extensions

import android.net.Uri
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toIntRect
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val AUTO_SCROLL_SPEED = 15f
private const val AUTO_SCROLL_INTERVAL_MS = 5L


internal fun Modifier.photoGridDragHandler(
    lazyGridState: LazyGridState,
    haptics: HapticFeedback,
    autoScrollThreshold: Float,
    onDragStart: (Uri) -> Unit = {},
    onDrag: (start: Int?, end: Int?) -> Unit = { _, _ -> },
    onDragEnd: () -> Unit = {}
) = pointerInput(Unit) {
    var initialKey: Uri? = null
    var initialIndex: Int? = null
    var currentKey: Uri? = null
    var scrollJob: Job? = null

    fun resetDrag() {
        scrollJob?.cancel()
        scrollJob = null
        initialKey = null
        initialIndex = null
        currentKey = null
        onDragEnd()
    }

    coroutineScope {
        val scope = this

        detectDragGesturesAfterLongPress(
            onDragStart = { offset ->
                val info = lazyGridState.gridItemKeyAtPosition(offset)
                    ?: return@detectDragGesturesAfterLongPress

                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                val key = info.key as? Uri ?: return@detectDragGesturesAfterLongPress

                initialKey = key
                initialIndex = info.index
                currentKey = key
                onDragStart(key)
            },
            onDragCancel = { resetDrag() },
            onDragEnd = { resetDrag() },
            onDrag = { change, _ ->
                if (initialKey == null) return@detectDragGesturesAfterLongPress

                val speed = autoScrollSpeed(
                    y = change.position.y,
                    viewportHeight = lazyGridState.layoutInfo.viewportSize.height,
                    threshold = autoScrollThreshold
                )
                scrollJob?.cancel()
                scrollJob = if (speed != 0f) {
                    scope.launch {
                        while (isActive) {
                            lazyGridState.scrollBy(speed)
                            delay(AUTO_SCROLL_INTERVAL_MS)
                        }
                    }
                } else null

                val info = lazyGridState.gridItemKeyAtPosition(change.position)
                    ?: return@detectDragGesturesAfterLongPress
                val key = info.key as? Uri ?: return@detectDragGesturesAfterLongPress

                if (currentKey != key) {
                    onDrag(initialIndex, info.index)
                    currentKey = key
                }
            }
        )
    }
}

/**
 * Returns the auto-scroll speed for the drag position: positive near the bottom edge,
 * negative near the top edge, and 0 otherwise.
 */
private fun autoScrollSpeed(y: Float, viewportHeight: Int, threshold: Float): Float = when {
    viewportHeight - y < threshold -> AUTO_SCROLL_SPEED
    y < threshold -> -AUTO_SCROLL_SPEED
    else -> 0f
}

internal fun LazyGridState.gridItemKeyAtPosition(hitPoint: Offset): LazyGridItemInfo? {
    return layoutInfo.visibleItemsInfo.find { itemInfo ->
        itemInfo.size.toIntRect().contains(hitPoint.round() - itemInfo.offset)
    }
}
