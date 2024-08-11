package com.github.panpf.zoomimage.compose.zoom.internal

import androidx.compose.runtime.Stable
import androidx.compose.ui.input.key.KeyEvent
import com.github.panpf.zoomimage.compose.util.KeyMatcher
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import kotlinx.coroutines.CoroutineScope

@Stable
interface ZoomKeyHandler {

    fun handle(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        event: KeyEvent
    ): Boolean
}

@Stable
abstract class MatcherZoomKeyHandler(
    open val keyMatchers: List<KeyMatcher>
) : ZoomKeyHandler {

    override fun handle(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        event: KeyEvent
    ): Boolean {
        var matched = false
        keyMatchers.forEach {
            if (!matched && it.match(event)) {
                onKey(coroutineScope, zoomableState, event)
                it.keyed = true
                matched = true
            } else if (it.keyed) {
                it.keyed = false
                onCanceled(coroutineScope, zoomableState, event)
            }
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