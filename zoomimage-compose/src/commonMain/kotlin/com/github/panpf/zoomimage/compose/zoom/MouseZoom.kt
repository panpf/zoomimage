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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import com.github.panpf.zoomimage.compose.internal.onPointerEvent
import com.github.panpf.zoomimage.zoom.GestureType
import kotlinx.coroutines.launch

// TODO Change to MouseScrollScaleElement
fun Modifier.mouseScrollScale(zoomableState: ZoomableState): Modifier = composed {
    val coroutineScope = rememberCoroutineScope()
    var pointerPosition by remember { mutableStateOf(Offset(0f, 0f)) }
    this.onPointerEvent(PointerEventType.Scroll) {
        if (zoomableState.disabledGestureTypes and GestureType.MOUSE_SCROLL_SCALE == 0) {
            coroutineScope.launch {
                val newScale =
                    zoomableState.transform.scaleX - (it.changes.first().scrollDelta.y * 0.33f)
                val contentPosition = zoomableState.touchPointToContentPoint(pointerPosition)
                zoomableState.scale(
                    newScale,
                    animated = true,
                    centroidContentPoint = contentPosition
                )
            }
        }
    }.onPointerEvent(PointerEventType.Move) {
        if (zoomableState.disabledGestureTypes and GestureType.MOUSE_SCROLL_SCALE == 0) {
            val position = it.changes.first().position
            pointerPosition = position
        }
    }
}