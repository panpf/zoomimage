/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
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

package com.github.panpf.zoomimage.compose.zoom.internal

import androidx.compose.runtime.Stable
import androidx.compose.ui.input.key.KeyEvent
import com.github.panpf.zoomimage.compose.util.KeyMatcher
import com.github.panpf.zoomimage.compose.util.MatcherKeyHandler
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import kotlinx.coroutines.CoroutineScope

/**
 * To facilitate handling of zoom key events, please use its implementation class [MatcherKeyHandler]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.zoom.internal.ZoomKeyHandlerTest
 */
@Stable
interface ZoomKeyHandler {

    fun handle(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        event: KeyEvent
    ): Boolean
}


/**
 * Match key events according to KeyMatcher, execute onKey when the match is successful,
 * execute onCanceled when the current match fails and the previous match was successful.
 *
 * @see com.github.panpf.zoomimage.compose.common.test.zoom.internal.ZoomKeyHandlerTest.testMatcherZoomKeyHandler
 */
@Stable
abstract class MatcherZoomKeyHandler(
    open val keyMatchers: List<KeyMatcher>
) : ZoomKeyHandler {

    private var lastMatched: Boolean = false

    override fun handle(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        event: KeyEvent
    ): Boolean {
        val matched = keyMatchers.any { it.match(event) }
        if (matched) {
            lastMatched = true
            onKey(coroutineScope, zoomableState, event)
        } else if (lastMatched) {
            lastMatched = false
            onCanceled(coroutineScope, zoomableState, event)
        }
        return matched
    }

    abstract fun onKey(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        event: KeyEvent
    )

    abstract fun onCanceled(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        event: KeyEvent
    )
}