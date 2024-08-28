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

package com.github.panpf.zoomimage.compose.zoom

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Immutable

/**
 * Animation-related configurations
 *
 * @see com.github.panpf.zoomimage.compose.common.test.zoom.ZoomAnimationSpecTest
 */
@Immutable
data class ZoomAnimationSpec(
    val durationMillis: Int = 300,
    val easing: Easing = FastOutSlowInEasing,
    val initialVelocity: Float = 0f,
) {
    companion object {
        val Default = ZoomAnimationSpec()
        val None = ZoomAnimationSpec(durationMillis = 0)
    }
}