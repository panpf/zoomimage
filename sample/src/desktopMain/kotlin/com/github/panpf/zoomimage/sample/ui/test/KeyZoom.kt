package com.github.panpf.zoomimage.sample.ui.test

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent

val MoveUp = TargetKey(Key.DirectionUp, isShiftPressed = true)
val MoveDown = TargetKey(Key.DirectionDown, isShiftPressed = true)
val MoveLeft = TargetKey(Key.DirectionLeft, isShiftPressed = true)
val MoveRight = TargetKey(Key.DirectionRight, isShiftPressed = true)

val ScaleUp = TargetKey(Key.DirectionUp, isAltPressed = true)
val ScaleDown = TargetKey(Key.DirectionDown, isAltPressed = true)

fun Modifier.keyZoom(
    moveUp: TargetKey = MoveUp,
    moveDown: TargetKey = MoveDown,
    moveLeft: TargetKey = MoveLeft,
    moveRight: TargetKey = MoveRight,
    scaleUp: TargetKey = ScaleUp,
    scaleDown: TargetKey = ScaleDown,
    onKeyEvent: (KeyEvent) -> Unit
): Modifier {
    return this.onPreviewKeyEvent {
        onKeyEvent(it)
        println("keyZoom onKeyEvent: $it")
        true
//        when {
//            moveUp.check(it) -> {
//                // TODO
//                true
//            }
//
//            moveDown.check(it) -> {
//                // TODO
//                true
//            }
//
//            moveLeft.check(it) -> {
//                // TODO
//                true
//            }
//
//            moveRight.check(it) -> {
//                // TODO
//                true
//            }
//
//            scaleUp.check(it) -> {
//                // TODO
//                true
//            }
//
//            scaleDown.check(it) -> {
//                // TODO
//                true
//            }
//
//            else -> false
//        }
    }
}

@Stable
data class TargetKey(
    val key: Key,
    val isShiftPressed: Boolean? = null,
    val isAltPressed: Boolean? = null,
    val isCtrlPressed: Boolean? = null,
    val isMetaPressed: Boolean? = null,
) {
    fun check(event: KeyEvent): Boolean {
        return event.key == key
                && (isShiftPressed == null || isShiftPressed == event.isShiftPressed)
                && (isAltPressed == null || isAltPressed == event.isAltPressed)
                && (isCtrlPressed == null || isCtrlPressed == event.isCtrlPressed)
                && (isMetaPressed == null || isMetaPressed == event.isMetaPressed)
    }
}