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

import android.content.Context
import android.widget.ImageView.ScaleType.FIT_CENTER
import com.github.panpf.zoomimage.sample.util.SettingsStateFlow
import com.github.panpf.zoomimage.subsampling.internal.TileManager
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.ScalesCalculator

class SettingsService(val context: Context) {

    private val preferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    val horizontalPagerLayout by lazy {
        SettingsStateFlow("horizontalPagerLayout", true, preferences)
    }

    val scaleType by lazy {
        SettingsStateFlow("scaleType", FIT_CENTER.name, preferences)
    }

    val contentScale by lazy {
        SettingsStateFlow("contentScale", "Fit", preferences)
    }
    val alignment by lazy {
        SettingsStateFlow("alignment", "Center", preferences)
    }

    val animateScale by lazy {
        SettingsStateFlow("animateScale", true, preferences)
    }
    val rubberBandScale by lazy {
        SettingsStateFlow("rubberBandScale", true, preferences)
    }
    val threeStepScale by lazy {
        SettingsStateFlow("threeStepScale", false, preferences)
    }
    val slowerScaleAnimation by lazy {
        SettingsStateFlow("slowerScaleAnimation", false, preferences)
    }
    val scalesCalculator by lazy {
        SettingsStateFlow("scalesCalculator", "Dynamic", preferences)
    }
    val scalesMultiple by lazy {
        SettingsStateFlow("scalesMultiple", ScalesCalculator.Multiple.toString(), preferences)
    }
    val disabledGestureType by lazy {
        SettingsStateFlow("disabledGestureType", "0", preferences)
    }

    val limitOffsetWithinBaseVisibleRect by lazy {
        SettingsStateFlow("limitOffsetWithinBaseVisibleRect", false, preferences)
    }

    val readModeEnabled by lazy {
        SettingsStateFlow("readModeEnabled", true, preferences)
    }
    val readModeAcceptedBoth by lazy {
        SettingsStateFlow("readModeAcceptedBoth", true, preferences)
    }

    val pausedContinuousTransformType by lazy {
        val initialize = TileManager.DefaultPausedContinuousTransformType.toString()
        SettingsStateFlow("pausedContinuousTransformType", initialize, preferences)
    }
    val disabledBackgroundTiles by lazy {
        SettingsStateFlow("disabledBackgroundTiles", false, preferences)
    }
    val showTileBounds by lazy {
        SettingsStateFlow("showTileBounds", false, preferences)
    }
    val tileAnimation by lazy {
        SettingsStateFlow("tileAnimation", true, preferences)
    }

    val scrollBarEnabled by lazy {
        SettingsStateFlow("scrollBarEnabled", true, preferences)
    }

    val logLevel by lazy {
        SettingsStateFlow("logLevel", defaultLogLevel(), preferences)
    }

    companion object {
        fun defaultLogLevel(): String =
            Logger.levelName(if (BuildConfig.DEBUG) Logger.DEBUG else Logger.INFO)
    }
}