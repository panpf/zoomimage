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

package com.github.panpf.zoomimage.view.zoom.internal

import android.view.View
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.view.zoom.ZoomAnimationSpec
import com.github.panpf.zoomimage.zoom.BaseZoomAnimationSpec
import com.github.panpf.zoomimage.zoom.internal.AnimationAdapter

class ViewAnimationAdapter(val view: View) : AnimationAdapter {

    private var lastAnimatable: FloatAnimatable? = null
    private var lastFlingAnimatable: FlingAnimatable? = null

    override fun startAnimation(
        animationSpec: BaseZoomAnimationSpec?,
        onProgress: (progress: Float) -> Unit,
        onEnd: () -> Unit,
    ) {
        val finalAnimationSpec = (animationSpec as? ZoomAnimationSpec) ?: ZoomAnimationSpec.Default
        val scaleAnimatable = FloatAnimatable(
            view = view,
            startValue = 0f,
            endValue = 1f,
            durationMillis = finalAnimationSpec.durationMillis,
            interpolator = finalAnimationSpec.interpolator,
            onUpdateValue = onProgress,
            onEnd = onEnd
        )
        lastAnimatable = scaleAnimatable
        scaleAnimatable.start()
    }

    override fun stopAnimation(): Boolean {
        val lastScaleAnimatable = lastAnimatable
        val result = lastScaleAnimatable?.running == true
        if (result) {
            lastScaleAnimatable?.stop()
        }
        return result
    }

    override fun startFlingAnimation(
        start: IntOffsetCompat,
        bounds: IntRectCompat?,
        velocity: IntOffsetCompat,
        onUpdateValue: (value: IntOffsetCompat) -> Unit,
        onEnd: () -> Unit
    ) {
        val flingAnimatable = FlingAnimatable(
            view = view,
            start = start,
            bounds = bounds,
            velocity = velocity,
            onUpdateValue = onUpdateValue,
            onEnd = onEnd
        )
        lastFlingAnimatable = flingAnimatable
        flingAnimatable.start()
    }

    override fun stopFlingAnimation(): Boolean {
        val lastFlingAnimatable = lastFlingAnimatable
        val result = lastFlingAnimatable?.running == true
        if (result) {
            lastFlingAnimatable?.stop()
        }
        return result
    }
}