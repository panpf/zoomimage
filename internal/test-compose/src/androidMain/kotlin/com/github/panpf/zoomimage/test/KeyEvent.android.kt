package com.github.panpf.zoomimage.test

import android.os.SystemClock
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType

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
    nativeKeyEvent = android.view.KeyEvent(
        /* downTime = */
        SystemClock.uptimeMillis(),
        /* eventTime = */
        SystemClock.uptimeMillis(),
        /* action = */
        if (type == KeyEventType.KeyDown) android.view.KeyEvent.ACTION_DOWN else android.view.KeyEvent.ACTION_UP,
        /* code = */
        key.keyCode.toInt(),
        /* repeat = */
        0,
        /* metaState = */
        (if (isCtrlPressed) android.view.KeyEvent.META_CTRL_ON else 0) or (if (isMetaPressed) android.view.KeyEvent.META_META_ON else 0) or (if (isAltPressed) android.view.KeyEvent.META_ALT_ON else 0) or (if (isShiftPressed) android.view.KeyEvent.META_SHIFT_ON else 0),
    )
)