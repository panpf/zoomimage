/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.zoomimage.compose.util

import androidx.compose.runtime.Stable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.unit.Density
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import kotlinx.coroutines.CoroutineScope


expect fun platformAssistKey(): AssistKey

@Stable
interface KeyHandler {

    fun handle(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        density: Density,
        event: KeyEvent
    ): Boolean
}

@Stable
abstract class BaseKeyHandler(
    open val keyMatchers: List<KeyMatcher>
) : KeyHandler {

    override fun handle(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        density: Density,
        event: KeyEvent
    ): Boolean {
        var matched = false
        keyMatchers.forEach {
            if (!matched && it.match(event)) {
                onKey(coroutineScope, zoomableState, density, event)
                it.keyed = true
                matched = true
            } else if (it.keyed) {
                it.keyed = false
                onCanceled(coroutineScope, zoomableState, density, event)
            }
        }
        return matched
    }

    abstract fun onKey(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        density: Density,
        event: KeyEvent
    )

    abstract fun onCanceled(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        density: Density,
        event: KeyEvent
    )
}

@Stable
data class KeyMatcher(
    val key: Key,
    val assistKey: AssistKey? = null
) {

    var keyed: Boolean = false

    fun match(event: KeyEvent): Boolean {
        return event.key == key && (assistKey == null || assistKey.check(event))
    }
}

@Stable
enum class AssistKey {
    Ctrl, Meta, Alt, Shift;

    fun check(event: KeyEvent): Boolean {
        return when (this) {
            Ctrl -> event.isCtrlPressed
            Meta -> event.isMetaPressed
            Alt -> event.isAltPressed
            Shift -> event.isShiftPressed
        }
    }
}