package com.github.panpf.zoomimage.sample.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalInspectionMode
import com.github.panpf.zoomimage.sample.compose.ui.ZoomImageOptionsState
import com.github.panpf.zoomimage.sample.ui.MySettings
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun rememberZoomImageOptionsState(): ZoomImageOptionsState {
    val state = remember { ZoomImageOptionsState() }
    if (!LocalInspectionMode.current) {
        val settingsService = MySettings
        BindStateAndFlow(state.contentScaleName, settingsService.contentScaleName)
        BindStateAndFlow(state.alignmentName, settingsService.alignmentName)

        BindStateAndFlow(state.animateScale, settingsService.animateScale)
        BindStateAndFlow(state.rubberBandScale, settingsService.rubberBandScale)
        BindStateAndFlow(state.threeStepScale, settingsService.threeStepScale)
        BindStateAndFlow(state.slowerScaleAnimation, settingsService.slowerScaleAnimation)
        BindStateAndFlow(state.scalesCalculator, settingsService.scalesCalculator)
        BindStateAndFlow(state.scalesMultiple, settingsService.scalesMultiple)
        BindStateAndFlow(
            state.limitOffsetWithinBaseVisibleRect,
            settingsService.limitOffsetWithinBaseVisibleRect
        )

        BindStateAndFlow(state.readModeEnabled, settingsService.readModeEnabled)
        BindStateAndFlow(state.readModeAcceptedBoth, settingsService.readModeAcceptedBoth)

        BindStateAndFlow(
            state.disabledGestureType,
            settingsService.disabledGestureType
        )
        BindStateAndFlow(
            state.pausedContinuousTransformType,
            settingsService.pausedContinuousTransformType
        )
        BindStateAndFlow(state.disabledBackgroundTiles, settingsService.disabledBackgroundTiles)
        BindStateAndFlow(state.showTileBounds, settingsService.showTileBounds)
        BindStateAndFlow(state.tileAnimation, settingsService.tileAnimation)

        BindStateAndFlow(state.scrollBarEnabled, settingsService.scrollBarEnabled)
        BindStateAndFlow(state.logLevel, settingsService.logLevel)
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