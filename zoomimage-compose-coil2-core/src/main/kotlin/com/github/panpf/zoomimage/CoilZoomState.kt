package com.github.panpf.zoomimage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.github.panpf.zoomimage.coil.CoilModelToImageSource
import com.github.panpf.zoomimage.compose.ZoomState
import com.github.panpf.zoomimage.compose.rememberZoomImageLogger
import com.github.panpf.zoomimage.compose.subsampling.SubsamplingState
import com.github.panpf.zoomimage.compose.subsampling.rememberSubsamplingState
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import com.github.panpf.zoomimage.util.Logger

/**
 * Creates and remember a [CoilZoomState]
 *
 * @see com.github.panpf.zoomimage.compose.coil2.core.test.CoilZoomStateTest.testRememberCoilZoomState
 */
@Composable
fun rememberCoilZoomState(logger: Logger = rememberZoomImageLogger(tag = "CoilZoomAsyncImage")): CoilZoomState {
    val zoomableState = rememberZoomableState(logger)
    val subsamplingState = rememberSubsamplingState(logger, zoomableState)
    return remember(logger, zoomableState, subsamplingState) {
        CoilZoomState(logger, zoomableState, subsamplingState)
    }
}

/**
 * A [ZoomState] implementation that supports Coil
 *
 * @see com.github.panpf.zoomimage.compose.coil2.core.test.CoilZoomStateTest
 */
@Stable
class CoilZoomState(
    logger: Logger,
    zoomable: ZoomableState,
    subsampling: SubsamplingState
) : ZoomState(logger, zoomable, subsampling) {

    private var _modelToImageSources = emptyList<CoilModelToImageSource>()
    val modelToImageSources: List<CoilModelToImageSource>
        get() = _modelToImageSources

    fun registerModelToImageSource(modelToImageSource: CoilModelToImageSource) {
        _modelToImageSources += modelToImageSource
    }

    fun unregisterModelToImageSource(modelToImageSource: CoilModelToImageSource) {
        _modelToImageSources -= modelToImageSource
    }
}