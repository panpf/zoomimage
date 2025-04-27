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

package com.github.panpf.zoomimage.test

import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.RectCompat
import com.github.panpf.zoomimage.util.limitTo
import com.github.panpf.zoomimage.zoom.BaseZoomAnimationSpec
import com.github.panpf.zoomimage.zoom.internal.AnimationAdapter
import kotlinx.coroutines.delay
import kotlin.time.TimeSource

class TestAnimationAdapter : AnimationAdapter {

    private var animationRunning: Boolean = false
    private var flingRunning: Boolean = false

    override suspend fun startAnimation(
        animationSpec: BaseZoomAnimationSpec?,
        onProgress: (progress: Float) -> Unit,
        onEnd: () -> Unit,
    ) {
        if (animationRunning) return
        animationRunning = true
        val finalAnimationSpec =
            (animationSpec as? TestZoomAnimationSpec) ?: TestZoomAnimationSpec.Default
        var progress: Float
        val startTime = TimeSource.Monotonic.markNow()
        try {
            while (animationRunning) {
                progress = startTime.elapsedNow().inWholeMilliseconds.toFloat()
                    .div(finalAnimationSpec.durationMillis)
                    .coerceIn(0f..1f)
                onProgress(progress)
                if (progress >= 1f) {
                    break
                } else {
                    delay(16)
                }
            }
        } finally {
            animationRunning = false
            onEnd()
        }
    }

    override suspend fun stopAnimation(): Boolean {
        val animationRunning = this.animationRunning
        if (animationRunning) {
            this.animationRunning = false
        }
        return animationRunning
    }

    override suspend fun startFlingAnimation(
        startUserOffset: OffsetCompat,
        userOffsetBounds: RectCompat?,
        velocity: OffsetCompat,
        extras: Map<String, Any>,
        onUpdateValue: (value: OffsetCompat) -> Boolean,
        onEnd: () -> Unit
    ) {
        if (flingRunning) return
        flingRunning = true
        val durationMillis = 300
        var progress: Float
        val addOffset = OffsetCompat(
            x = if (velocity.x > 0) 100f else -100f,
            y = if (velocity.y > 0) 100f else -100f
        )
        val startTime = TimeSource.Monotonic.markNow()
        var userOffset = startUserOffset
        try {
            while (flingRunning) {
                progress = startTime.elapsedNow().inWholeMilliseconds.toFloat()
                    .div(durationMillis)
                    .coerceIn(0f..1f)
                val newUserOffset = userOffset + addOffset
                userOffset = if (userOffsetBounds != null) {
                    newUserOffset.limitTo(userOffsetBounds)
                } else {
                    newUserOffset
                }
                if (!onUpdateValue(userOffset)) {
                    break
                } else if (progress >= 1f) {
                    break
                } else {
                    delay(16)
                }
            }
        } finally {
            flingRunning = false
            onEnd()
        }
    }

    override suspend fun stopFlingAnimation(): Boolean {
        val flingRunning = this.flingRunning
        if (flingRunning) {
            this.flingRunning = false
        }
        return flingRunning
    }
}