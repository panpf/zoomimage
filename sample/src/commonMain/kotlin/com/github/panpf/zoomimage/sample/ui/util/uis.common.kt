package com.github.panpf.zoomimage.sample.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize

@Composable
expect fun windowSize(): IntSize

/**
 * Create a modifier for processing pointer input within the region of the modified element.
 *
 * Calls [onEvent] when a [PointerEvent] with type [eventType] is reported to the
 * specified input [pass].
 *
 * Note that this function is experimental, and can be replaced in the future
 * by high-level functions similar to `clickable`, `hoverable`, etc.
 */
@ExperimentalComposeUiApi
fun Modifier.onPointerEvent(
    eventType: PointerEventType,
    pass: PointerEventPass = PointerEventPass.Main,
    onEvent: AwaitPointerEventScope.(event: PointerEvent) -> Unit
): Modifier = composed {
    val currentEventType by rememberUpdatedState(eventType)
    val currentOnEvent by rememberUpdatedState(onEvent)
    pointerInput(pass) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(pass)
                if (event.type == currentEventType) {
                    currentOnEvent(event)
                }
            }
        }
    }
}