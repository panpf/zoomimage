package com.github.panpf.zoomimage.internal

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.coroutineScope


suspend fun PointerInputScope.detectTwoDowns(onTowDownsChange: (towDowns: Boolean) -> Unit) =
    coroutineScope {
        awaitPointerEventScope {
            awaitTwoDowns(false)
        }
        // todo There is a bug, this one can be corrected and the new transformable can be used
        awaitEachGesture {
            onTowDownsChange(false)
            onTowDownsChange(true)
//            val down = awaitFirstDown(requireUnconsumed = false)
//            down.consume()
//            onTowDownsChange(false)
//
//            var twoDowns = false
//            do {
//                val event: PointerEvent = awaitPointerEvent()
//                val newTwoDowns = event.changes.size > 1
//                if (newTwoDowns != twoDowns) {
//                    twoDowns = newTwoDowns
//                    onTowDownsChange(twoDowns)
//                }
//                val canceled = event.changes.any { it.isConsumed }
//            } while (!canceled)
        }
    }

//private suspend fun AwaitPointerEventScope.awaitSecondDown(
//    firstUp: PointerInputChange
//): PointerInputChange? = withTimeoutOrNull(viewConfiguration.doubleTapTimeoutMillis) {
//    val minUptime = firstUp.uptimeMillis + viewConfiguration.doubleTapMinTimeMillis
//    var change: PointerInputChange
//    // The second tap doesn't count if it happens before DoubleTapMinTime of the first tap
//    do {
//        change = awaitFirstDown()
//    } while (change.uptimeMillis < minUptime)
//    change
//}

/**
 * Reads events until the first down is received. If [requireUnconsumed] is `true` and the first
 * down is consumed in the [PointerEventPass.Main] pass, that gesture is ignored.
 */
private suspend fun AwaitPointerEventScope.awaitTwoDowns(requireUnconsumed: Boolean = true) {
    var event: PointerEvent
    var firstDown: PointerId? = null
    do {
        event = awaitPointerEvent()
        var downPointers = if (firstDown != null) 1 else 0
        event.changes.fastForEach {
            val isDown =
                if (requireUnconsumed) it.changedToDown() else it.changedToDownIgnoreConsumed()
            val isUp =
                if (requireUnconsumed) it.changedToUp() else it.changedToUpIgnoreConsumed()
            if (isUp && firstDown == it.id) {
                firstDown = null
                downPointers -= 1
            }
            if (isDown) {
                firstDown = it.id
                downPointers += 1
            }
        }
        val satisfied = downPointers > 1
    } while (!satisfied)
}