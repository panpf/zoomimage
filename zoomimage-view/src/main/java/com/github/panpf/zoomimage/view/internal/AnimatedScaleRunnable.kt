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
package com.github.panpf.zoomimage.view.internal

import androidx.core.view.ViewCompat

internal class AnimatedScaleRunnable(
    private val engine: ZoomEngine,
    private val scaleDragHelper: ScaleDragHelper,
    private val startScale: Float,
    private val endScale: Float,
    private val scaleFocalX: Float,
    private val scaleFocalY: Float
) : Runnable {

    private val startTime: Long = System.currentTimeMillis()

    var isRunning = false
        private set

    fun start() {
        isRunning = true
        engine.view.post(this)
    }

    fun cancel() {
        engine.view.removeCallbacks(this)
        isRunning = false
    }

    override fun run() {
        val t = interpolate()
        val newScale = startScale + t * (endScale - startScale)
        val currentScale = scaleDragHelper.scale
        val deltaScale = newScale / currentScale
        isRunning = t < 1f
        scaleDragHelper.doScale(deltaScale, scaleFocalX, scaleFocalY, 0f, 0f)
        // We haven't hit our target scale yet, so post ourselves again
        if (isRunning) {
            ViewCompat.postOnAnimation(engine.view, this)
        }
    }

    private fun interpolate(): Float {
        var t = 1f * (System.currentTimeMillis() - startTime) / engine.animationSpec.durationMillis
        t = 1f.coerceAtMost(t)
        t = engine.animationSpec.interpolator.getInterpolation(t)
        return t
    }
}