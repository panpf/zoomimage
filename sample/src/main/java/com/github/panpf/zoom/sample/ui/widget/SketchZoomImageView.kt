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
package com.github.panpf.zoom.sample.ui.widget

import android.content.Context
import android.content.ContextWrapper
import android.util.AttributeSet
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.github.panpf.sketch.request.DisplayListenerProvider
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.DisplayResult
import com.github.panpf.sketch.request.ImageOptions
import com.github.panpf.sketch.request.ImageOptionsProvider
import com.github.panpf.sketch.request.Listener
import com.github.panpf.sketch.request.ProgressListener
import com.github.panpf.sketch.request.isSketchGlobalLifecycle
import com.github.panpf.zoom.SubsamplingImageView

open class SketchZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : SubsamplingImageView(context, attrs, defStyle), ImageOptionsProvider, DisplayListenerProvider {

    // todo 恢复 Ability 支持

    override var displayImageOptions: ImageOptions? = null
    private val listener =
        object : Listener<DisplayRequest, DisplayResult.Success, DisplayResult.Error> {
            override fun onStart(request: DisplayRequest) {
                super.onStart(request)
                subsamplingAbility.lifecycle =
                    request.lifecycle.takeIf { !it.isSketchGlobalLifecycle() }
                        ?: context.getLifecycle()
            }
        }

    override fun getDisplayListener(): Listener<DisplayRequest, DisplayResult.Success, DisplayResult.Error>? {
        return listener
    }

    override fun getDisplayProgressListener(): ProgressListener<DisplayRequest>? {
        return null
    }

    internal fun Context?.getLifecycle(): Lifecycle? {
        var context: Context? = this
        while (true) {
            when (context) {
                is LifecycleOwner -> return context.lifecycle
                is ContextWrapper -> context = context.baseContext
                else -> return null
            }
        }
    }
}