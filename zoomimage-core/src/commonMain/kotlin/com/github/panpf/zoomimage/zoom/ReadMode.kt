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

import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.format
import com.github.panpf.zoomimage.util.times
import kotlin.math.max

/**
 * Read mode allows long text images to be displayed in a way that fills the screen at the beginning and is positioned at the beginning of the image,
 * so that users can directly start read the content of the image without double-clicking to enlarge and then sliding to the beginning position
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ReadModeTest
 */
data class ReadMode(
    /**
     * Limit the size types of image that can use read mode.
     * Both the default horizontal and vertical image are available
     */
    val sizeType: Int = SIZE_TYPE_HORIZONTAL or SIZE_TYPE_VERTICAL,

    /**
     * Determines whether the image can use read mode
     */
    val decider: Decider = Decider.Default
) {

    /**
     * Based on contentSize and containerSize, determine whether read mode can be used
     */
    fun accept(contentSize: IntSizeCompat, containerSize: IntSizeCompat): Boolean {
        val accepted = contentSize.width == contentSize.height
                || (sizeType and SIZE_TYPE_HORIZONTAL != 0 && contentSize.width > contentSize.height)
                || (sizeType and SIZE_TYPE_VERTICAL != 0 && contentSize.width < contentSize.height)
        val should = decider.should(contentSize = contentSize, containerSize = containerSize)
        return accepted && should
    }

    companion object {
        /**
         * Horizontal image
         */
        const val SIZE_TYPE_HORIZONTAL = 1

        /**
         * Vertical image
         */
        const val SIZE_TYPE_VERTICAL = 2

        /**
         * Default read mode
         */
        val Default = ReadMode(
            sizeType = SIZE_TYPE_HORIZONTAL or SIZE_TYPE_VERTICAL,
            decider = Decider.Default
        )
    }

    /**
     * Determines whether the image can use read mode
     */
    interface Decider {

        fun should(contentSize: IntSizeCompat, containerSize: IntSizeCompat): Boolean

        companion object {
            val Default = LongImageDecider()
        }
    }

    /**
     * When contentSize and containerSize are in the same direction,
     * read mode can be used when the difference multiple is greater than [sameDirectionMultiple],
     * otherwise read mode can be used when the difference multiple is greater than [notSameDirectionMultiple]
     *
     * @see com.github.panpf.zoomimage.core.common.test.zoom.LongImageDeciderTest
     */
    class LongImageDecider(
        val sameDirectionMultiple: Float = 2.5f,
        val notSameDirectionMultiple: Float = 5.0f,
    ) : Decider {

        override fun should(contentSize: IntSizeCompat, containerSize: IntSizeCompat): Boolean {
            val fillScale = max(
                containerSize.width / contentSize.width.toFloat(),
                containerSize.height / contentSize.height.toFloat()
            )
            val filledSrcSize = contentSize.times(ScaleFactorCompat(fillScale))
            val maxScaleMultiple = max(
                filledSrcSize.width / containerSize.width.toFloat(),
                filledSrcSize.height / containerSize.height.toFloat()
            )
            val sameDirection = isSameDirection(srcSize = contentSize, dstSize = containerSize)
            val minMultiple = if (sameDirection) sameDirectionMultiple else notSameDirectionMultiple
            return maxScaleMultiple.format(1) >= minMultiple.format(1)
        }

        private fun isSameDirection(srcSize: IntSizeCompat, dstSize: IntSizeCompat): Boolean {
            val srcAspectRatio = srcSize.width.toFloat().div(srcSize.height).format(2)
            val dstAspectRatio = dstSize.width.toFloat().div(dstSize.height).format(2)
            return (srcAspectRatio == 1.0f || dstAspectRatio == 1.0f)
                    || (srcAspectRatio > 1.0f && dstAspectRatio > 1.0f)
                    || (srcAspectRatio < 1.0f && dstAspectRatio < 1.0f)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as LongImageDecider
            if (sameDirectionMultiple != other.sameDirectionMultiple) return false
            if (notSameDirectionMultiple != other.notSameDirectionMultiple) return false
            return true
        }

        override fun hashCode(): Int {
            var result = sameDirectionMultiple.hashCode()
            result = 31 * result + notSameDirectionMultiple.hashCode()
            return result
        }

        override fun toString(): String {
            return "LongImageDecider($sameDirectionMultiple:$notSameDirectionMultiple)"
        }
    }
}