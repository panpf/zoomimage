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

// TODO KeyZoom: Shift plus Arrow key to move, Short press to move, long press to move continuously
// TODO KeyZoom: Ctrl plus Up and Down key to scale, Short press to scale, long press to scale continuously
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
//                true
//            }
//
//            moveDown.check(it) -> {
//                true
//            }
//
//            moveLeft.check(it) -> {
//                true
//            }
//
//            moveRight.check(it) -> {
//                true
//            }
//
//            scaleUp.check(it) -> {
//                true
//            }
//
//            scaleDown.check(it) -> {
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