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

package com.github.panpf.zoomimage.view.zoom

import com.github.panpf.zoomimage.zoom.HapticFeedback
import com.github.panpf.zoomimage.zoom.OneFingerScaleSpec
import com.github.panpf.zoomimage.zoom.PanToScaleTransformer
import kotlinx.coroutines.delay

fun OneFingerScaleSpec.Companion.heartbeat(
    zoomableEngine: ZoomableEngine,
    panToScaleTransformer: PanToScaleTransformer = PanToScaleTransformer.Default
): OneFingerScaleSpec {
    return OneFingerScaleSpec(
        hapticFeedback = HeartbeatHapticFeedback(zoomableEngine),
        panToScaleTransformer = panToScaleTransformer
    )
}

data class HeartbeatHapticFeedback(
    private val zoomableEngine: ZoomableEngine
) : HapticFeedback {

    override suspend fun perform() {
        val currentScale = zoomableEngine.transformState.value.scale.scaleX
        zoomableEngine.scale(
            targetScale = currentScale * 1.2f,
            animated = true,
            animationSpec = ZoomAnimationSpec(durationMillis = 150)
        )
        delay(150)
        zoomableEngine.scale(
            targetScale = currentScale,
            animated = true,
            animationSpec = ZoomAnimationSpec(durationMillis = 150)
        )
    }
}