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

package com.github.panpf.zoomimage.compose.zoom

import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

expect fun checkAssistKeyPressed(event: KeyEvent): Boolean

// TODO test
class KeyboardZoomer(
    val keyHandlers: List<KeyHandler> = listOf(
        ScaleUpKeyHandler(),
        ScaleDownKeyHandler(),
        MoveUpKeyHandler(),
        MoveDownKeyHandler(),
        MoveLeftKeyHandler(),
        MoveRightKeyHandler(),
    )
) {
    fun handle(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        event: KeyEvent
    ): Boolean = keyHandlers.any {
        it.handle(coroutineScope, zoomableState, event)
    }
}

@Stable
interface KeyHandler {

    fun handle(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        event: KeyEvent
    ): Boolean
}

class ScaleUpKeyHandler : KeyHandler {

    override fun handle(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        event: KeyEvent
    ): Boolean {
        if (event.key == Key.Equals && checkAssistKeyPressed(event)) {
            // TODO Improved zoom step, and continuous zoom
            coroutineScope.launch {
                zoomableState.scale(
                    targetScale = zoomableState.transform.scaleX + 0.5f,
                    animated = true
                )
            }
            return true
        }
        return false
    }
}

class ScaleDownKeyHandler : KeyHandler {

    override fun handle(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        event: KeyEvent
    ): Boolean {
        if (event.key == Key.Minus && checkAssistKeyPressed(event)) {
            coroutineScope.launch {
                zoomableState.scale(
                    targetScale = zoomableState.transform.scaleX - 0.5f,
                    animated = true
                )
            }
            return true
        }
        return false
    }
}

class MoveUpKeyHandler : KeyHandler {

    override fun handle(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        event: KeyEvent
    ): Boolean {
        if (event.key == Key.DirectionUp && checkAssistKeyPressed(event)) {
            // TODO Improved displacement step, and continuous displacement
            coroutineScope.launch {
                zoomableState.offset(
                    targetOffset = zoomableState.transform.offset + Offset(0f, 100f),
                    animated = true
                )
            }
            return true
        }
        return false
    }
}

class MoveDownKeyHandler : KeyHandler {

    override fun handle(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        event: KeyEvent
    ): Boolean {
        if (event.key == Key.DirectionDown && checkAssistKeyPressed(event)) {
            coroutineScope.launch {
                zoomableState.offset(
                    targetOffset = zoomableState.transform.offset + Offset(0f, -100f),
                    animated = true
                )
            }
            return true
        }
        return false
    }
}

class MoveLeftKeyHandler : KeyHandler {

    override fun handle(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        event: KeyEvent
    ): Boolean {
        if (event.key == Key.DirectionLeft && checkAssistKeyPressed(event)) {
            coroutineScope.launch {
                zoomableState.offset(
                    targetOffset = zoomableState.transform.offset + Offset(100f, 0f),
                    animated = true
                )
            }
            return true
        }
        return false
    }
}

class MoveRightKeyHandler : KeyHandler {

    override fun handle(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        event: KeyEvent
    ): Boolean {
        if (event.key == Key.DirectionRight && checkAssistKeyPressed(event)) {
            coroutineScope.launch {
                zoomableState.offset(
                    targetOffset = zoomableState.transform.offset + Offset(-100f, 0f),
                    animated = true
                )
            }
            return true
        }
        return false
    }
}