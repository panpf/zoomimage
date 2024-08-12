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