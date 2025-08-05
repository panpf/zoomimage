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

package com.github.panpf.zoomimage.compose.zoom.internal

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.animation.splineBasedDecay
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import com.github.panpf.zoomimage.compose.util.toCompat
import com.github.panpf.zoomimage.compose.util.toPlatform
import com.github.panpf.zoomimage.compose.zoom.ZoomAnimationSpec
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.RectCompat
import com.github.panpf.zoomimage.zoom.BaseZoomAnimationSpec
import com.github.panpf.zoomimage.zoom.internal.AnimationAdapter
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class ComposeAnimationAdapter : AnimationAdapter {

    private var lastAnimatable: Animatable<*, *>? = null
    private var lastFlingAnimatable: Animatable<*, *>? = null

    override fun isRunning(): Boolean {
        return lastAnimatable?.isRunning == true
    }

    override suspend fun startAnimation(
        animationSpec: BaseZoomAnimationSpec?,
        onProgress: (progress: Float) -> Unit,
        onEnd: () -> Unit
    ) {
        val updateAnimatable = Animatable(0f)
        lastAnimatable = updateAnimatable
        val finalAnimationSpec = (animationSpec as? ZoomAnimationSpec) ?: ZoomAnimationSpec.Default
        updateAnimatable.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = finalAnimationSpec.durationMillis,
                easing = finalAnimationSpec.easing
            ),
            initialVelocity = finalAnimationSpec.initialVelocity,
        ) {
            onProgress(value)
        }
    }

    override suspend fun stopAnimation(): Boolean {
        val lastScaleAnimatable = lastAnimatable
        val result = lastScaleAnimatable?.isRunning == true
        if (result) {
            lastScaleAnimatable.stop()
        }
        return result
    }

    override fun isFlingRunning(): Boolean {
        return lastFlingAnimatable?.isRunning == true
    }

    override suspend fun startFlingAnimation(
        startUserOffset: OffsetCompat,
        userOffsetBounds: RectCompat?,
        velocity: OffsetCompat,
        extras: Map<String, Any>,
        onUpdateValue: (value: OffsetCompat) -> Boolean,
        onEnd: () -> Unit
    ) {
        val flingAnimatable = Animatable(
            initialValue = startUserOffset.toPlatform(),
            typeConverter = Offset.VectorConverter,
        )
        lastFlingAnimatable = flingAnimatable
        val initialVelocity = Offset.VectorConverter
            .convertFromVector(AnimationVector(velocity.x, velocity.y))
        val density = extras["density"] as Density
        coroutineScope {
            val scope = this
            flingAnimatable.animateDecay(
                initialVelocity = initialVelocity,
                animationSpec = splineBasedDecay(density)
            ) {
                if (!onUpdateValue(this.value.toCompat())) {
                    // SubsamplingState(line 87) relies on the fling state to refresh tiles,
                    // so you need to end the fling animation as soon as possible
                    scope.launch {
                        flingAnimatable.stop()
                    }
                }
            }
        }
    }

    override suspend fun stopFlingAnimation(): Boolean {
        val lastFlingAnimatable = lastFlingAnimatable
        val result = lastFlingAnimatable?.isRunning == true
        if (result) {
            lastFlingAnimatable.stop()
        }
        return result
    }
}