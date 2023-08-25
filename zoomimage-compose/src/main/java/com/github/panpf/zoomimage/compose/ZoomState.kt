package com.github.panpf.zoomimage.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.compose.subsampling.SubsamplingState
import com.github.panpf.zoomimage.compose.subsampling.rememberSubsamplingState
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState

@Composable
fun rememberZoomState(logger: Logger = rememberZoomImageLogger()): ZoomState {
    val zoomableState = rememberZoomableState(logger)
    val subsamplingState = rememberSubsamplingState(logger)
    subsamplingState.BindZoomableState(zoomableState)
    return remember { ZoomState(logger, zoomableState, subsamplingState) }
}

@Stable
class ZoomState(
    val logger: Logger,
    val zoomable: ZoomableState,
    val subsampling: SubsamplingState,
) {

    override fun toString(): String {
        return "ZoomState(${zoomable}, ${subsampling})"
    }
}