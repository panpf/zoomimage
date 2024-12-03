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

expect val PlatformContext.appSettings: AppSettings

expect val composeImageLoaders: List<ImageLoaderSettingItem>

@Composable
expect fun getComposeImageLoaderIcon(composeImageLoader: String): Painter

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class AppSettings(context: PlatformContext) {

    // ---------------------------------------- ZoomImage ------------------------------------------

    val contentScaleName: SettingsStateFlow<String>
    val contentScale: StateFlow<ContentScaleCompat>

    val alignmentName: SettingsStateFlow<String>
    val alignment: StateFlow<AlignmentCompat>

    val animateScale: SettingsStateFlow<Boolean>

    val rubberBandScale: SettingsStateFlow<Boolean>

    val threeStepScale: SettingsStateFlow<Boolean>

    val slowerScaleAnimation: SettingsStateFlow<Boolean>

    val reverseMouseWheelScale: SettingsStateFlow<Boolean>

    val scalesCalculatorName: SettingsStateFlow<String>
    val scalesMultiple: SettingsStateFlow<String>
    // stateCombine will cause UI lag
//    val scalesCalculator: StateFlow<ScalesCalculator>

    val disabledGestureTypes: SettingsStateFlow<Int>

    val limitOffsetWithinBaseVisibleRect: SettingsStateFlow<Boolean>

    val containerWhitespaceMultiple: SettingsStateFlow<Float>

    val containerWhitespace: SettingsStateFlow<Boolean>

    val readModeEnabled: SettingsStateFlow<Boolean>

    val readModeAcceptedBoth: SettingsStateFlow<Boolean>

    val pausedContinuousTransformTypes: SettingsStateFlow<Int>

    val disabledBackgroundTiles: SettingsStateFlow<Boolean>

    val showTileBounds: SettingsStateFlow<Boolean>

    val tileAnimation: SettingsStateFlow<Boolean>

    val tileMemoryCache: SettingsStateFlow<Boolean>

    val scrollBarEnabled: SettingsStateFlow<Boolean>


    // ------------------------------------------ other --------------------------------------------

    val composeImageLoader: SettingsStateFlow<String>

    val currentPageIndex: SettingsStateFlow<Int>

    val horizontalPagerLayout: SettingsStateFlow<Boolean>

    val staggeredGridMode: SettingsStateFlow<Boolean>

    val logLevelName: SettingsStateFlow<String>
    val logLevel: StateFlow<Logger.Level>

    val debugLog: SettingsStateFlow<Boolean>

    val pagerGuideShowed: SettingsStateFlow<Boolean>
}

fun buildScalesCalculator(scalesCalculatorName: String, scalesMultiple: Float): ScalesCalculator {
    return if (scalesCalculatorName == "Dynamic") {
        ScalesCalculator.dynamic(scalesMultiple)
    } else {
        ScalesCalculator.fixed(scalesMultiple)
    }
}