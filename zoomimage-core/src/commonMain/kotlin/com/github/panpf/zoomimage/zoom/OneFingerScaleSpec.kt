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

package com.github.panpf.zoomimage.zoom

/**
 * One finger double-click and slide up and down to scale the configuration of the image
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.OneFingerScaleSpecTest
 */
data class OneFingerScaleSpec(
    val panToScaleTransformer: PanToScaleTransformer = PanToScaleTransformer.Default
) {

    companion object {
        /**
         * Default configuration, no haptic feedback
         */
        val Default = OneFingerScaleSpec()
    }
}

interface PanToScaleTransformer {

    companion object {
        val Default = DefaultPanToScaleTransformer()
    }

    fun transform(panY: Float): Float
}

/**
 * @see com.github.panpf.zoomimage.core.common.test.zoom.DefaultPanToScaleTransformerTest
 */
class DefaultPanToScaleTransformer(val reference: Int = 200) : PanToScaleTransformer {

    override fun transform(panY: Float): Float {
        return 1f + (panY / reference)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as DefaultPanToScaleTransformer
        return reference == other.reference
    }

    override fun hashCode(): Int {
        return reference
    }

    override fun toString(): String {
        return "DefaultPanToScaleTransformer(reference=$reference)"
    }
}