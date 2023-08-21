package com.github.panpf.zoomimage.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.ReadMode
import com.github.panpf.zoomimage.compose.subsampling.BindZoomableStateAndSubsamplingState
import com.github.panpf.zoomimage.compose.subsampling.SubsamplingState
import com.github.panpf.zoomimage.compose.subsampling.rememberSubsamplingState
import com.github.panpf.zoomimage.compose.zoom.ZoomAnimationSpec
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import com.github.panpf.zoomimage.util.DefaultMediumScaleMinMultiple

@Composable
fun rememberZoomState(
    mediumScaleMinMultiple: Float = DefaultMediumScaleMinMultiple,
    threeStepScale: Boolean = false,
    rubberBandScale: Boolean = true,
    animationSpec: ZoomAnimationSpec = ZoomAnimationSpec.Default,
    readMode: ReadMode? = null,
    showTileBounds: Boolean = false,
    logger: Logger = rememberZoomImageLogger()
): ZoomState {
    val zoomableState = rememberZoomableState(
        logger = logger,
        mediumScaleMinMultiple = mediumScaleMinMultiple,
        threeStepScale = threeStepScale,
        rubberBandScale = rubberBandScale,
        animationSpec = animationSpec,
        readMode = readMode,
    )

    val subsamplingState = rememberSubsamplingState(
        logger = logger,
        showTileBounds = showTileBounds
    )

    BindZoomableStateAndSubsamplingState(zoomableState, subsamplingState)

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