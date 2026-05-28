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
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.zoomimage.sample.ui.model.ImageLoaderSettingItem
import com.github.panpf.zoomimage.sample.ui.util.name
import com.github.panpf.zoomimage.sample.util.SettingsStateFlow
import com.github.panpf.zoomimage.sample.util.booleanSettingsStateFlow
import com.github.panpf.zoomimage.sample.util.enumSettingsStateFlow
import com.github.panpf.zoomimage.sample.util.floatSettingsStateFlow
import com.github.panpf.zoomimage.sample.util.intSettingsStateFlow
import com.github.panpf.zoomimage.sample.util.stateMap
import com.github.panpf.zoomimage.sample.util.stringSettingsStateFlow
import com.github.panpf.zoomimage.subsampling.internal.TileManager
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import com.github.panpf.zoomimage.zoom.valueOf
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


    val imageLoaderLogLevelName: SettingsStateFlow<String>
    val imageLoaderLogLevel: StateFlow<com.github.panpf.sketch.util.Logger.Level>

    val zoomImageLogLevelName: SettingsStateFlow<String>
    val zoomImageLogLevel: StateFlow<Logger.Level>

    val darkMode: SettingsStateFlow<DarkMode>
}

enum class DarkMode {
    SYSTEM, LIGHT, DARK
}

expect fun platformSupportedDarkModes(): List<DarkMode>

fun buildScalesCalculator(scalesCalculatorName: String, scalesMultiple: Float): ScalesCalculator {
    return if (scalesCalculatorName == "Dynamic") {
        ScalesCalculator.dynamic(scalesMultiple)
    } else {
        ScalesCalculator.fixed(scalesMultiple)
    }
}

abstract class BaseAppSettings(val context: PlatformContext) {

    /* ------------------------------------------ Content Arrange -------------------------------------------- */

    val contentScaleName: SettingsStateFlow<String> by lazy {
        stringSettingsStateFlow(context, "contentScale", ContentScale.Fit.name)
    }
    val contentScale: StateFlow<ContentScaleCompat> =
        contentScaleName.stateMap { ContentScaleCompat.valueOf(it) }

    val alignmentName: SettingsStateFlow<String> by lazy {
        stringSettingsStateFlow(context, "alignment", Alignment.Center.name)
    }
    val alignment: StateFlow<AlignmentCompat> =
        alignmentName.stateMap { AlignmentCompat.valueOf(it) }

    val rtlLayoutDirectionEnabled: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "rtlLayoutDirectionEnabled", false)
    }

    /* ------------------------------------------ Zoom Common -------------------------------------------- */

    val zoomAnimateEnabled: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "zoomAnimateEnabled", true)
    }

    val zoomSlowerAnimationEnabled: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "zoomSlowerAnimationEnabled", false)
    }

    val readModeEnabled: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "readModeEnabled", true)
    }

    val readModeAcceptedBoth: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "readModeAcceptedBoth", true)
    }

    val scrollBarEnabled: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "scrollBarEnabled", true)
    }

    val keepTransformEnabled: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "keepTransformEnabled", true)
    }

    val disabledGestureTypes: SettingsStateFlow<Int> by lazy {
        intSettingsStateFlow(context, "disabledGestureTypes", 0)
    }


    /* ------------------------------------------ Zoom Scale -------------------------------------------- */

    val rubberBandScaleEnabled: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "rubberBandScaleEnabled", true)
    }

    val threeStepScaleEnabled: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "threeStepScaleEnabled", false)
    }

    val reverseMouseWheelScaleEnabled: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "reverseMouseWheelScaleEnabled", false)
    }

    val scalesCalculatorName: SettingsStateFlow<String> by lazy {
        stringSettingsStateFlow(context, "scalesCalculator", "Dynamic")
    }
    val fixedScalesCalculatorMultiple: SettingsStateFlow<String> by lazy {
        stringSettingsStateFlow(
            context,
            "fixedScalesCalculatorMultiple",
            ScalesCalculator.MULTIPLE.toString()
        )
    }
    // stateCombine will cause UI lag
//    val scalesCalculator: StateFlow<ScalesCalculator> =
//        stateCombine(listOf(scalesCalculatorName, scalesMultiple)) {
//            val scalesCalculatorName: String = it[0]
//            val scalesMultiple: Float = it[1].toFloat()
//            buildScalesCalculator(scalesCalculatorName, scalesMultiple)
//        }


    /* ------------------------------------------ Zoom Offset -------------------------------------------- */

    val limitOffsetWithinBaseVisibleRect: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "limitOffsetWithinBaseVisibleRect", false)
    }

    val containerWhitespaceEnabled: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "containerWhitespaceEnabled", false)
    }

    val containerWhitespaceMultiple: SettingsStateFlow<Float> by lazy {
        floatSettingsStateFlow(context, "containerWhitespaceMultiple1", 0f)
    }


    /* ------------------------------------------ Subsampling -------------------------------------------- */

    val subsamplingEnabled: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "subsamplingEnabled", true)
    }

    val tileAnimationEnabled: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "tileAnimationEnabled", true)
    }

    val tileBoundsEnabled: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "tileBoundsEnabled", false)
    }

    val backgroundTilesEnabled: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "backgroundTilesEnabled", true)
    }

    val tileMemoryCacheEnabled: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "tileMemoryCacheEnabled", true)
    }

    val autoStopWithLifecycleEnabled: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "autoStopWithLifecycleEnabled", true)
    }

    val pausedContinuousTransformTypes by lazy {
        val initialize = TileManager.DefaultPausedContinuousTransformTypes
        intSettingsStateFlow(context, "pausedContinuousTransformTypes", initialize)
    }


    /* ------------------------------------------ Other -------------------------------------------- */

    val currentPageIndex: SettingsStateFlow<Int> by lazy {
        intSettingsStateFlow(context, "currentPageIndex", 0)
    }

    val staggeredGridMode: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "staggeredGridMode", false)
    }

    val composeImageLoader: SettingsStateFlow<String> by lazy {
        stringSettingsStateFlow(context, "composeImageLoader", composeImageLoaders.first().name)
    }


    val pagerGuideShowed: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "pagerGuideShowed", false)
    }

    val horizontalPagerLayout: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "horizontalPagerLayout", true)
    }

    val delayImageLoadEnabled: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "delayImageLoadEnabled", false)
    }


    val imageLoaderLogLevelName: SettingsStateFlow<String> by lazy {
        stringSettingsStateFlow(context, "imageLoaderLogLevel", Logger.Level.Warn.name)
    }
    val imageLoaderLogLevel: StateFlow<com.github.panpf.sketch.util.Logger.Level> =
        imageLoaderLogLevelName.stateMap { com.github.panpf.sketch.util.Logger.Level.valueOf(it) }


    val zoomImageLogLevelName: SettingsStateFlow<String> by lazy {
        stringSettingsStateFlow(context, "zoomImageLogLevel", Logger.Level.Debug.name)
    }
    val zoomImageLogLevel: StateFlow<Logger.Level> =
        zoomImageLogLevelName.stateMap { Logger.Level.valueOf(it) }

    val darkMode: SettingsStateFlow<DarkMode> by lazy {
        enumSettingsStateFlow(
            context = context,
            key = "darkMode",
            initialize = platformSupportedDarkModes().first(),
            convert = DarkMode::valueOf
        )
    }
}