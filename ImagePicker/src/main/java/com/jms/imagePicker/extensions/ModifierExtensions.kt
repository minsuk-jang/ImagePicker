package com.jms.imagePicker.extensions

import android.net.Uri
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.State
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


internal fun Modifier.photoGridDragHandler(
    lazyGridState: LazyGridState,
    selectedUris: State<List<Uri>>,
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
                lazyGridState.gridItemKeyAtPosition(offset)?.let { info ->
                    val key = info.key as? Uri
                    val index = info.index

                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (key == null) return@detectDragGesturesAfterLongPress

                    if (!selectedUris.value.contains(key)) {
                        initialKey = key
                        initialIndex = index
                        currentKey = key
                        onDragStart(key)
                    }
                }
            },
            onDragCancel = { resetDrag() },
            onDragEnd = { resetDrag() },
            onDrag = { change, _ ->
                if (initialKey != null) {
                    val distFromBottom =
                        lazyGridState.layoutInfo.viewportSize.height - change.position.y
                    val distFromTop = change.position.y

                    val scrollSpeed = when {
                        distFromBottom < autoScrollThreshold -> 15f
                        distFromTop < autoScrollThreshold -> -15f
                        else -> 0f
                    }

                    scrollJob?.cancel()
                    scrollJob = if (scrollSpeed != 0f) {
                        scope.launch {
                            while (isActive) {
                                lazyGridState.scrollBy(scrollSpeed)
                                delay(5)
                            }
                        }
                    } else null

                    lazyGridState.gridItemKeyAtPosition(change.position)?.let { info ->
                        val key = info.key as? Uri
                        val index = info.index

                        if (key == null) return@detectDragGesturesAfterLongPress

                        if (currentKey != key) {
                            onDrag(initialIndex, index)
                            currentKey = key
                        }
                    }
                }
            }
        )
    }
}

internal fun LazyGridState.gridItemKeyAtPosition(hitPoint: Offset): LazyGridItemInfo? {
    return layoutInfo.visibleItemsInfo.find { itemInfo ->
        itemInfo.size.toIntRect().contains(hitPoint.round() - itemInfo.offset)
    }
}
