package com.github.panpf.zoomimage.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.compose.subsampling.SubsamplingState
import com.github.panpf.zoomimage.compose.subsampling.rememberSubsamplingState
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState

/**
 * Creates and remember a [ZoomState]
 */
@Composable
fun rememberZoomState(): ZoomState {
    val logger = rememberZoomImageLogger()
    val zoomableState = rememberZoomableState(logger)
    val subsamplingState = rememberSubsamplingState(logger)
    subsamplingState.BindZoomableState(zoomableState)
    return remember { ZoomState(logger, zoomableState, subsamplingState) }
}

/**
 * Used to control the state of scaling, translation, rotation, and subsampling
 */
@Stable
class ZoomState(
    /**
     * Used to print log
     */
    val logger: Logger,

    /**
     * Used to control the state of scaling, translation, and rotation
     */
    val zoomable: ZoomableState,

    /**
     * Used to control the state of subsampling
     */
    val subsampling: SubsamplingState,
) {

    override fun toString(): String {
        return "ZoomState(logger=${logger}, zoomable=${zoomable}, subsampling=${subsampling})"
    }
}