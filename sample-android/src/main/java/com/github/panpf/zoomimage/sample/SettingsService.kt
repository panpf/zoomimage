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
import com.github.panpf.zoomimage.sample.util.BooleanMmkvData
import com.github.panpf.zoomimage.sample.util.StringMmkvData
import com.github.panpf.zoomimage.subsampling.internal.TileManager
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import com.tencent.mmkv.MMKV

class SettingsService(val context: Context) {

    private val mmkv = MMKV.defaultMMKV()

    val horizontalPagerLayout by lazy {
        BooleanMmkvData(mmkv, "horizontalPagerLayout", true)
    }

    val scaleType by lazy {
        StringMmkvData(mmkv, "scaleType", FIT_CENTER.name)
    }

    val contentScale by lazy {
        StringMmkvData(mmkv, "contentScale", "Fit")
    }
    val alignment by lazy {
        StringMmkvData(mmkv, "alignment", "Center")
    }

    val animateScale by lazy {
        BooleanMmkvData(mmkv, "animateScale", true)
    }
    val rubberBandScale by lazy {
        BooleanMmkvData(mmkv, "rubberBandScale", true)
    }
    val threeStepScale by lazy {
        BooleanMmkvData(mmkv, "threeStepScale", false)
    }
    val oneFingerScale by lazy {
        BooleanMmkvData(mmkv, "oneFingerScale", false)
    }
    val slowerScaleAnimation by lazy {
        BooleanMmkvData(mmkv, "slowerScaleAnimation", false)
    }
    val scalesCalculator by lazy {
        StringMmkvData(mmkv, "scalesCalculator", "Dynamic")
    }
    val scalesMultiple by lazy {
        StringMmkvData(mmkv, "scalesMultiple", ScalesCalculator.Multiple.toString())
    }
    val disabledGestureType by lazy {
        StringMmkvData(mmkv, "disabledGestureType", "0")
    }

    val limitOffsetWithinBaseVisibleRect by lazy {
        BooleanMmkvData(mmkv, "limitOffsetWithinBaseVisibleRect", false)
    }

    val readModeEnabled by lazy {
        BooleanMmkvData(mmkv, "readModeEnabled", true)
    }
    val readModeAcceptedBoth by lazy {
        BooleanMmkvData(mmkv, "readModeAcceptedBoth", true)
    }

    val pausedContinuousTransformType by lazy {
        StringMmkvData(
            mmkv,
            "pausedContinuousTransformType",
            TileManager.DefaultPausedContinuousTransformType.toString()
        )
    }
    val disabledBackgroundTiles by lazy {
        BooleanMmkvData(mmkv, "disabledBackgroundTiles", false)
    }
    val ignoreExifOrientation by lazy {
        BooleanMmkvData(mmkv, "ignoreExifOrientation", false)
    }
    val showTileBounds by lazy {
        BooleanMmkvData(mmkv, "showTileBounds", false)
    }
    val tileAnimation by lazy {
        BooleanMmkvData(mmkv, "tileAnimation", true)
    }

    val scrollBarEnabled by lazy {
        BooleanMmkvData(mmkv, "scrollBarEnabled", true)
    }

    val logLevel by lazy {
        StringMmkvData(mmkv, "logLevel", defaultLogLevel())
    }

    companion object {
        fun defaultLogLevel(): String =
            Logger.levelName(if (BuildConfig.DEBUG) Logger.DEBUG else Logger.INFO)
    }
}