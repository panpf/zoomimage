/*
 * Copyright (C) 2022 panpf <panpfpanpf@outlook.com>
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
import com.tencent.mmkv.MMKV

class PrefsService(val context: Context) {

    private val mmkv = MMKV.defaultMMKV()

    val scaleType by lazy {
        StringMmkvData(mmkv, "scaleType", FIT_CENTER.name)
    }
    val disableMemoryCache by lazy {
        BooleanMmkvData(mmkv, "disableMemoryCache", false)
    }
    val disallowReuseBitmap by lazy {
        BooleanMmkvData(mmkv, "disallowReuseBitmap", false)
    }
    val ignoreExifOrientation by lazy {
        BooleanMmkvData(mmkv, "ignoreExifOrientation", false)
    }
    val scrollBarEnabled by lazy {
        BooleanMmkvData(mmkv, "scrollBarEnabled", true)
    }
    val readModeEnabled by lazy {
        BooleanMmkvData(mmkv, "readModeEnabled", true)
    }
    val showTileBounds by lazy {
        BooleanMmkvData(mmkv, "showTileBounds", false)
    }
}