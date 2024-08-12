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