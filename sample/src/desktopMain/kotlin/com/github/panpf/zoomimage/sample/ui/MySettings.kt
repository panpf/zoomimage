package com.github.panpf.zoomimage.sample.ui

import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import com.github.panpf.zoomimage.sample.ui.util.name
import com.github.panpf.zoomimage.subsampling.internal.TileManager
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import kotlinx.coroutines.flow.MutableStateFlow

object MySettings {
    val contentScaleName = MutableStateFlow(ContentScale.Fit.name)
    val alignmentName = MutableStateFlow(Alignment.Center.name)

    val animateScale = MutableStateFlow(true)
    val rubberBandScale = MutableStateFlow(true)
    val threeStepScale = MutableStateFlow(false)
    val slowerScaleAnimation = MutableStateFlow(false)
    val scalesCalculator = MutableStateFlow("Dynamic")
    val scalesMultiple = MutableStateFlow(ScalesCalculator.Multiple.toString())
    val limitOffsetWithinBaseVisibleRect = MutableStateFlow(false)

    val readModeEnabled = MutableStateFlow(true)
    val readModeAcceptedBoth = MutableStateFlow(true)

    val showTileBounds = MutableStateFlow(false)
    val tileAnimation = MutableStateFlow(true)
    val pausedContinuousTransformType =
        MutableStateFlow(TileManager.DefaultPausedContinuousTransformType.toString())
    val disabledGestureType = MutableStateFlow(0.toString())
    val disabledBackgroundTiles = MutableStateFlow(false)

    val scrollBarEnabled = MutableStateFlow(true)
    val logLevel = MutableStateFlow(Logger.levelName(Logger.DEBUG))
}