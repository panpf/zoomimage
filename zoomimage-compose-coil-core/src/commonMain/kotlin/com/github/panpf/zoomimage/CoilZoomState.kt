package com.github.panpf.zoomimage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.github.panpf.zoomimage.coil.CoilModelToImageSource
import com.github.panpf.zoomimage.coil.CoilModelToImageSourceImpl
import com.github.panpf.zoomimage.compose.ZoomState
import com.github.panpf.zoomimage.compose.rememberZoomImageLogger
import com.github.panpf.zoomimage.compose.subsampling.SubsamplingState
import com.github.panpf.zoomimage.compose.subsampling.rememberSubsamplingState
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import com.github.panpf.zoomimage.util.Logger
import kotlinx.collections.immutable.ImmutableList

/**
 * Creates and remember a [CoilZoomState]
 *
 * @see com.github.panpf.zoomimage.compose.coil.core.test.CoilZoomStateTest.testRememberCoilZoomState
 */
@Composable
fun rememberCoilZoomState(
    modelToImageSources: ImmutableList<CoilModelToImageSource>? = null,
): CoilZoomState {
    val logger: Logger = rememberZoomImageLogger(tag = "CoilZoomAsyncImage")
    val zoomableState = rememberZoomableState(logger)
    val subsamplingState = rememberSubsamplingState(zoomableState)
    return remember(logger, zoomableState, subsamplingState, modelToImageSources) {
        CoilZoomState(logger, zoomableState, subsamplingState, modelToImageSources)
    }
}

/**
 * A [ZoomState] implementation that supports Coil
 *
 * @see com.github.panpf.zoomimage.compose.coil.core.test.CoilZoomStateTest
 */
@Stable
class CoilZoomState(
    logger: Logger,
    zoomable: ZoomableState,
    subsampling: SubsamplingState,
    modelToImageSources: ImmutableList<CoilModelToImageSource>?
) : ZoomState(logger, zoomable, subsampling) {
    val modelToImageSources: List<CoilModelToImageSource> =
        modelToImageSources.orEmpty().plus(CoilModelToImageSourceImpl())
}