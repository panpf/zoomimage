package com.github.panpf.zoomimage.test

import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType

@OptIn(InternalComposeUiApi::class)
actual fun KeyEvent(
    key: Key,
    type: KeyEventType,
    codePoint: Int,
    isCtrlPressed: Boolean,
    isMetaPressed: Boolean,
    isAltPressed: Boolean,
    isShiftPressed: Boolean,
    nativeEvent: Any?
): KeyEvent = KeyEvent(
    key = key,
    type = type,
    codePoint = codePoint,
    isCtrlPressed = isCtrlPressed,
    isMetaPressed = isMetaPressed,
    isAltPressed = isAltPressed,
    isShiftPressed = isShiftPressed,
    nativeEvent = nativeEvent
)