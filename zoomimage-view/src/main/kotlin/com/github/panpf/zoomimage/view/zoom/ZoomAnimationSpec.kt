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

package com.github.panpf.zoomimage.view.zoom

import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator

/**
 * Animation-related configurations
 *
 * @see com.github.panpf.zoomimage.view.test.zoom.ZoomAnimationSpecTest
 */
data class ZoomAnimationSpec(
    var durationMillis: Int = DEFAULT_DURATION_MILLIS,
    var interpolator: Interpolator = DEFAULT_INTERPOLATOR
) {
    companion object {
        val DEFAULT_DURATION_MILLIS = 300

        val DEFAULT_INTERPOLATOR = AccelerateDecelerateInterpolator()

        val Default = ZoomAnimationSpec(
            durationMillis = DEFAULT_DURATION_MILLIS,
            interpolator = DEFAULT_INTERPOLATOR,
        )

        val None = ZoomAnimationSpec(
            durationMillis = 0,
            interpolator = DEFAULT_INTERPOLATOR,
        )
    }
}