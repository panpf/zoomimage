package com.github.panpf.zoomimage.sample.ui.util

import androidx.compose.ui.input.key.KeyEvent
import kotlinx.coroutines.flow.MutableSharedFlow

object EventBus {
    val keyEvent = MutableSharedFlow<KeyEvent>()
}