package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import com.github.panpf.zoomimage.sample.compose.ui.ZoomImageOptionsState
import com.github.panpf.zoomimage.sample.settingsService
import com.github.panpf.zoomimage.sample.util.SettingsStateFlow
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun rememberZoomImageOptionsState(): ZoomImageOptionsState {
    val state = remember { ZoomImageOptionsState() }

    if (!LocalInspectionMode.current) {
        val settingsService = LocalContext.current.settingsService
        BindStateAndFlow(state.contentScaleName, settingsService.contentScale)
        BindStateAndFlow(state.alignmentName, settingsService.alignment)

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
        BindStateAndFlow(state.ignoreExifOrientation, settingsService.ignoreExifOrientation)
        BindStateAndFlow(state.showTileBounds, settingsService.showTileBounds)
        BindStateAndFlow(state.tileAnimation, settingsService.tileAnimation)

        BindStateAndFlow(state.scrollBarEnabled, settingsService.scrollBarEnabled)
        BindStateAndFlow(state.logLevel, settingsService.logLevel)
    }

    return state
}

@Composable
private fun <T> BindStateAndFlow(state: MutableStateFlow<T>, mmkvData: SettingsStateFlow<T>) {
    LaunchedEffect(state) {
        state.value = mmkvData.value
        state.collect {
            mmkvData.value = it
        }
    }
}