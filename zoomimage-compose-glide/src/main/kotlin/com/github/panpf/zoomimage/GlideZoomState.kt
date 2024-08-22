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
import com.github.panpf.zoomimage.glide.GlideModelToImageSource
import com.github.panpf.zoomimage.glide.GlideModelToImageSourceImpl
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.Logger.Level
import kotlinx.collections.immutable.ImmutableList

/**
 * Creates and remember a [GlideZoomState]
 *
 * @see com.github.panpf.zoomimage.compose.glide.test.GlideZoomStateTest.testRememberGlideZoomState
 */
@Composable
fun rememberGlideZoomState(
    modelToImageSources: ImmutableList<GlideModelToImageSource>? = null,
    logLevel: Level? = null,
): GlideZoomState {
    val logger: Logger = rememberZoomImageLogger(tag = "GlideZoomAsyncImage", level = logLevel)
    val zoomableState = rememberZoomableState(logger)
    val subsamplingState = rememberSubsamplingState(zoomableState)
    return remember(logger, zoomableState, subsamplingState, modelToImageSources) {
        GlideZoomState(logger, zoomableState, subsamplingState, modelToImageSources)
    }
}

/**
 * A [ZoomState] implementation that supports Glide
 *
 * @see com.github.panpf.zoomimage.compose.glide.test.GlideZoomStateTest
 */
@Stable
class GlideZoomState(
    logger: Logger,
    zoomable: ZoomableState,
    subsampling: SubsamplingState,
    modelToImageSources: ImmutableList<GlideModelToImageSource>?
) : ZoomState(logger, zoomable, subsampling) {

    val modelToImageSources: List<GlideModelToImageSource> =
        modelToImageSources.orEmpty().plus(GlideModelToImageSourceImpl())
}