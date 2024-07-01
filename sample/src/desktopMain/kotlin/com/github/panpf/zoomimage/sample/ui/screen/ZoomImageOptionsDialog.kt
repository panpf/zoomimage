package com.github.panpf.zoomimage.sample.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalInspectionMode
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.zoomimage.sample.appSettings
import com.github.panpf.zoomimage.sample.ui.ZoomImageOptionsState
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun rememberZoomImageOptionsState(): ZoomImageOptionsState {
    val state = remember { ZoomImageOptionsState() }
    if (!LocalInspectionMode.current) {
        val appSettings = LocalPlatformContext.current.appSettings
        BindStateAndFlow(state.contentScaleName, appSettings.contentScale)
        BindStateAndFlow(state.alignmentName, appSettings.alignment)

        BindStateAndFlow(state.animateScale, appSettings.animateScale)
        BindStateAndFlow(state.rubberBandScale, appSettings.rubberBandScale)
        BindStateAndFlow(state.threeStepScale, appSettings.threeStepScale)
        BindStateAndFlow(state.slowerScaleAnimation, appSettings.slowerScaleAnimation)
        BindStateAndFlow(state.scalesCalculator, appSettings.scalesCalculator)
        BindStateAndFlow(state.scalesMultiple, appSettings.scalesMultiple)
        BindStateAndFlow(
            state.limitOffsetWithinBaseVisibleRect,
            appSettings.limitOffsetWithinBaseVisibleRect
        )

        BindStateAndFlow(state.readModeEnabled, appSettings.readModeEnabled)
        BindStateAndFlow(state.readModeAcceptedBoth, appSettings.readModeAcceptedBoth)

        BindStateAndFlow(
            state.disabledGestureType,
            appSettings.disabledGestureType
        )
        BindStateAndFlow(
            state.pausedContinuousTransformType,
            appSettings.pausedContinuousTransformType
        )
        BindStateAndFlow(state.disabledBackgroundTiles, appSettings.disabledBackgroundTiles)
        BindStateAndFlow(state.showTileBounds, appSettings.showTileBounds)
        BindStateAndFlow(state.tileAnimation, appSettings.tileAnimation)

        BindStateAndFlow(state.scrollBarEnabled, appSettings.scrollBarEnabled)
        BindStateAndFlow(state.logLevel, appSettings.logLevel)
    }
    return state
}

@Composable
private fun <T> BindStateAndFlow(state: MutableStateFlow<T>, other: MutableStateFlow<T>) {
    LaunchedEffect(state, other) {
        state.value = other.value
        state.collect {
            other.value = it
        }
    }
}