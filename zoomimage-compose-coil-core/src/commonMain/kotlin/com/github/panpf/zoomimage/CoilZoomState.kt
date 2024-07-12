package com.github.panpf.zoomimage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.github.panpf.zoomimage.compose.ZoomState
import com.github.panpf.zoomimage.compose.rememberZoomImageLogger
import com.github.panpf.zoomimage.compose.subsampling.SubsamplingState
import com.github.panpf.zoomimage.compose.subsampling.rememberSubsamplingState
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import com.github.panpf.zoomimage.util.Logger

/**
 * Creates and remember a [CoilZoomState]
 */
@Composable
fun rememberCoilZoomState(logger: Logger = rememberZoomImageLogger(tag = "CoilZoomAsyncImage")): CoilZoomState {
    val zoomableState = rememberZoomableState(logger)
    val subsamplingState = rememberSubsamplingState(logger, zoomableState)
    return remember(logger, zoomableState, subsamplingState) {
        CoilZoomState(logger, zoomableState, subsamplingState)
    }
}

@Stable
class CoilZoomState(logger: Logger, zoomable: ZoomableState, subsampling: SubsamplingState) :
    ZoomState(logger, zoomable, subsampling)