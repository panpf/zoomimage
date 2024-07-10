/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
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
import com.github.panpf.zoomimage.sample.ui.util.name
import com.github.panpf.zoomimage.sample.util.ImageLoaderSettingItem
import com.github.panpf.zoomimage.sample.util.booleanSettingsStateFlow
import com.github.panpf.zoomimage.sample.util.intSettingsStateFlow
import com.github.panpf.zoomimage.sample.util.stringSettingsStateFlow
import com.github.panpf.zoomimage.subsampling.internal.TileManager
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.ScalesCalculator

expect val PlatformContext.appSettings: AppSettings

expect fun isDebugMode(): Boolean

expect val composeImageLoaders: List<ImageLoaderSettingItem>

@Composable
expect fun getComposeImageLoaderIcon(composeImageLoader: String): Painter

class AppSettings(val context: PlatformContext) {

    // Only for Android
    val composePage by lazy {
        booleanSettingsStateFlow(context, "composePage", true)
    }

    val composeImageLoader by lazy {
        stringSettingsStateFlow(context, "composeImageLoader", composeImageLoaders.first().name)
    }

    // Only for Android
    val viewImageLoader by lazy {
        stringSettingsStateFlow(context, "viewImageLoader", "Sketch")
    }

    val currentPageIndex by lazy {
        intSettingsStateFlow(context, "currentPageIndex", 0)
    }
    val horizontalPagerLayout by lazy {
        booleanSettingsStateFlow(context, "horizontalPagerLayout", true)
    }
    val staggeredGridMode by lazy {
        booleanSettingsStateFlow(
            context = context,
            key = "staggeredGridMode",
            initialize = false,
        )
    }

    val contentScale by lazy {
        stringSettingsStateFlow(context, "contentScale", ContentScale.Fit.name)
    }
    val alignment by lazy {
        stringSettingsStateFlow(context, "alignment", Alignment.Center.name)
    }

    val animateScale by lazy {
        booleanSettingsStateFlow(context, "animateScale", true)
    }
    val rubberBandScale by lazy {
        booleanSettingsStateFlow(context, "rubberBandScale", true)
    }
    val threeStepScale by lazy {
        booleanSettingsStateFlow(context, "threeStepScale", false)
    }
    val slowerScaleAnimation by lazy {
        booleanSettingsStateFlow(context, "slowerScaleAnimation", false)
    }
    val scalesCalculator by lazy {
        stringSettingsStateFlow(context, "scalesCalculator", "Dynamic")
    }
    val scalesMultiple by lazy {
        stringSettingsStateFlow(context, "scalesMultiple", ScalesCalculator.MULTIPLE.toString())
    }
    val disabledGestureType by lazy {
        intSettingsStateFlow(context, "disabledGestureTypeInt", 0)
    }

    val limitOffsetWithinBaseVisibleRect by lazy {
        booleanSettingsStateFlow(context, "limitOffsetWithinBaseVisibleRect", false)
    }

    val readModeEnabled by lazy {
        booleanSettingsStateFlow(context, "readModeEnabled", true)
    }
    val readModeAcceptedBoth by lazy {
        booleanSettingsStateFlow(context, "readModeAcceptedBoth", true)
    }

    val pausedContinuousTransformType by lazy {
        val initialize = TileManager.DefaultPausedContinuousTransformType
        intSettingsStateFlow(context, "pausedContinuousTransformTypeInt", initialize)
    }
    val disabledBackgroundTiles by lazy {
        booleanSettingsStateFlow(context, "disabledBackgroundTiles", false)
    }
    val showTileBounds by lazy {
        booleanSettingsStateFlow(context, "showTileBounds", false)
    }
    val tileAnimation by lazy {
        booleanSettingsStateFlow(context, "tileAnimation", true)
    }

    val scrollBarEnabled by lazy {
        booleanSettingsStateFlow(context, "scrollBarEnabled", true)
    }

    val logLevel by lazy {
        stringSettingsStateFlow(context, "logLevel", defaultLogLevel())
    }

    val debugLog by lazy {
        booleanSettingsStateFlow(context, "debugLog", isDebugMode())
    }

    companion object {
        fun defaultLogLevel(): String =
            Logger.levelName(if (isDebugMode()) Logger.DEBUG else Logger.INFO)
    }
}