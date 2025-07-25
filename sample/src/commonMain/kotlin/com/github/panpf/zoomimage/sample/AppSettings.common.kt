/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.zoomimage.sample

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.zoomimage.sample.ui.model.ImageLoaderSettingItem
import com.github.panpf.zoomimage.sample.util.SettingsStateFlow
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import kotlinx.coroutines.flow.StateFlow

expect val composeImageLoaders: List<ImageLoaderSettingItem>

@Composable
expect fun getComposeImageLoaderIcon(composeImageLoader: String): Painter

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class AppSettings(context: PlatformContext) {

    /* ------------------------------------------ Content Arrange -------------------------------------------- */

    val contentScaleName: SettingsStateFlow<String>
    val contentScale: StateFlow<ContentScaleCompat>

    val alignmentName: SettingsStateFlow<String>
    val alignment: StateFlow<AlignmentCompat>

    val rtlLayoutDirectionEnabled: SettingsStateFlow<Boolean>


    /* ------------------------------------------ Zoom Common -------------------------------------------- */

    val zoomAnimateEnabled: SettingsStateFlow<Boolean>

    val zoomSlowerAnimationEnabled: SettingsStateFlow<Boolean>

    val disabledGestureTypes: SettingsStateFlow<Int>

    val keepTransformEnabled: SettingsStateFlow<Boolean>


    /* ------------------------------------------ Zoom Scale -------------------------------------------- */

    val rubberBandScaleEnabled: SettingsStateFlow<Boolean>

    val threeStepScaleEnabled: SettingsStateFlow<Boolean>

    val reverseMouseWheelScaleEnabled: SettingsStateFlow<Boolean>

    val scalesCalculatorName: SettingsStateFlow<String>
    val fixedScalesCalculatorMultiple: SettingsStateFlow<String>
    // stateCombine will cause UI lag
//    val scalesCalculator: StateFlow<ScalesCalculator>


    /* ------------------------------------------ Zoom Offset -------------------------------------------- */

    val rubberBandOffsetEnabled: SettingsStateFlow<Boolean>

    val alwaysCanDragAtEdgeEnabled: SettingsStateFlow<Boolean>

    val limitOffsetWithinBaseVisibleRect: SettingsStateFlow<Boolean>

    val containerWhitespaceMultiple: SettingsStateFlow<Float>

    val containerWhitespaceEnabled: SettingsStateFlow<Boolean>


    /* ------------------------------------------ Zoom Read Mode -------------------------------------------- */

    val readModeEnabled: SettingsStateFlow<Boolean>

    val readModeAcceptedBoth: SettingsStateFlow<Boolean>


    /* ------------------------------------------ Subsampling -------------------------------------------- */

    val subsamplingEnabled: SettingsStateFlow<Boolean>

    val tileAnimationEnabled: SettingsStateFlow<Boolean>

    val tileBoundsEnabled: SettingsStateFlow<Boolean>

    val backgroundTilesEnabled: SettingsStateFlow<Boolean>

    val tileMemoryCacheEnabled: SettingsStateFlow<Boolean>

    val autoStopWithLifecycleEnabled: SettingsStateFlow<Boolean>

    val pausedContinuousTransformTypes: SettingsStateFlow<Int>


    /* ------------------------------------------ Scroll Bar -------------------------------------------- */

    val scrollBarEnabled: SettingsStateFlow<Boolean>


    /* ------------------------------------------ Other -------------------------------------------- */

    val currentPageIndex: SettingsStateFlow<Int>

    val staggeredGridMode: SettingsStateFlow<Boolean>

    val composeImageLoader: SettingsStateFlow<String>


    val pagerGuideShowed: SettingsStateFlow<Boolean>

    val horizontalPagerLayout: SettingsStateFlow<Boolean>

    val delayImageLoadEnabled: SettingsStateFlow<Boolean>


    val logLevelName: SettingsStateFlow<String>
    val logLevel: StateFlow<Logger.Level>

    val debugLog: SettingsStateFlow<Boolean>
}

fun buildScalesCalculator(scalesCalculatorName: String, scalesMultiple: Float): ScalesCalculator {
    return if (scalesCalculatorName == "Dynamic") {
        ScalesCalculator.dynamic(scalesMultiple)
    } else {
        ScalesCalculator.fixed(scalesMultiple)
    }
}