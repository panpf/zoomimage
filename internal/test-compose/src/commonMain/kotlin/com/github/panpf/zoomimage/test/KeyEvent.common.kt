package com.github.panpf.zoomimage.test

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType

expect fun KeyEvent(
    key: Key,
    type: KeyEventType,
    codePoint: Int = 0,
    isCtrlPressed: Boolean = false,
    isMetaPressed: Boolean = false,
    isAltPressed: Boolean = false,
    isShiftPressed: Boolean = false,
    nativeEvent: Any? = null
): KeyEvent


fun eventADown(
    ctrl: Boolean = false,
    meta: Boolean = false,
    alt: Boolean = false,
    shift: Boolean = false,
    nativeEvent: Any? = null
) = com.github.panpf.zoomimage.test.KeyEvent(
    key = Key.A,
    type = KeyEventType.KeyDown,
    codePoint = 0,
    isCtrlPressed = ctrl,
    isMetaPressed = meta,
    isAltPressed = alt,
    isShiftPressed = shift,
    nativeEvent = nativeEvent
)

fun eventAUp(
    ctrl: Boolean = false,
    meta: Boolean = false,
    alt: Boolean = false,
    shift: Boolean = false,
    nativeEvent: Any? = null
) = com.github.panpf.zoomimage.test.KeyEvent(
    key = Key.A,
    type = KeyEventType.KeyUp,
    codePoint = 0,
    isCtrlPressed = ctrl,
    isMetaPressed = meta,
    isAltPressed = alt,
    isShiftPressed = shift,
    nativeEvent = nativeEvent
)

fun eventBDown(
    ctrl: Boolean = false,
    meta: Boolean = false,
    alt: Boolean = false,
    shift: Boolean = false,
    nativeEvent: Any? = null
) = com.github.panpf.zoomimage.test.KeyEvent(
    key = Key.B,
    type = KeyEventType.KeyDown,
    codePoint = 0,
    isCtrlPressed = ctrl,
    isMetaPressed = meta,
    isAltPressed = alt,
    isShiftPressed = shift,
    nativeEvent = nativeEvent
)

fun eventBUp(
    ctrl: Boolean = false,
    meta: Boolean = false,
    alt: Boolean = false,
    shift: Boolean = false,
    nativeEvent: Any? = null
) = com.github.panpf.zoomimage.test.KeyEvent(
    key = Key.B,
    type = KeyEventType.KeyUp,
    codePoint = 0,
    isCtrlPressed = ctrl,
    isMetaPressed = meta,
    isAltPressed = alt,
    isShiftPressed = shift,
    nativeEvent = nativeEvent
)