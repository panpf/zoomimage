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

import com.github.panpf.sketch.PlatformContext
import com.github.panpf.zoomimage.sample.util.ParamLazy
import com.github.panpf.zoomimage.sample.util.booleanSettingsStateFlow
import com.github.panpf.zoomimage.sample.util.intSettingsStateFlow
import com.github.panpf.zoomimage.sample.util.stringSettingsStateFlow
import com.github.panpf.zoomimage.subsampling.internal.TileManager
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.ScalesCalculator

private val appSettingsLazy = ParamLazy<PlatformContext, AppSettings> { AppSettings(it) }

val PlatformContext.appSettings: AppSettings
    get() = appSettingsLazy.get(this)

expect fun isDebugMode(): Boolean

class AppSettings(val context: PlatformContext) {

    // Only for Android
    val composePage by lazy {
        booleanSettingsStateFlow(context, "composePage", true)
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

    val scaleType by lazy {
        stringSettingsStateFlow(context, "scaleType", "FIT_CENTER")
    }

    val contentScale by lazy {
        stringSettingsStateFlow(context, "contentScale", "Fit")
    }
    val alignment by lazy {
        stringSettingsStateFlow(context, "alignment", "Center")
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
        stringSettingsStateFlow(context, "disabledGestureType", "0")
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
        val initialize = TileManager.DefaultPausedContinuousTransformType.toString()
        stringSettingsStateFlow(context, "pausedContinuousTransformType", initialize)
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