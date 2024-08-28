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
import android.view.animation.Interpolator

/**
 * A simple float animation class that can animate a float value from a start value to an end value.
 *
 * @see com.github.panpf.zoomimage.view.test.zoom.internal.FloatAnimatableTest
 */
internal class FloatAnimatable(
    private val view: View,
    private val startValue: Float,
    private val endValue: Float,
    private val durationMillis: Int,
    private val interpolator: Interpolator,
    private val onUpdateValue: (value: Float) -> Unit,
    private val onEnd: () -> Unit = {}
) {

    private val runnable = Runnable { frame() }
    private var startTime = 0L

    var value = startValue
        private set
    var running = false
        private set

    fun start(delay: Int = 0) {
        if (running) return
        running = true
        value = startValue
        startTime = System.currentTimeMillis() + delay
        view.postDelayed(runnable, delay.toLong())
    }

    fun stop() {
        if (!running) return
        running = false
        view.removeCallbacks(runnable)
        onEnd()
    }

    fun restart(delay: Int = 0) {
        stop()
        start(delay)
    }

    private fun frame() {
        if (!running) return
        val progress = computeProgress()
        val currentValue = startValue + (progress * (endValue - startValue))
        value = currentValue
        onUpdateValue(currentValue)
        if (progress < 1f) {
            view.postOnAnimation(runnable)
        } else {
            running = false
            onEnd()
        }
    }

    private fun computeProgress(): Float {
        if (durationMillis <= 0) return 1f
        val elapsedTime = (System.currentTimeMillis() - startTime).coerceAtLeast(0)
        val progress = (elapsedTime.toFloat() / durationMillis).coerceIn(0f, 1f)
        val changedProgress = interpolator.getInterpolation(progress)
        return changedProgress
    }
}