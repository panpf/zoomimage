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
package com.github.panpf.zoomimage

import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.util.internal.format
import com.github.panpf.zoomimage.util.isSameDirection
import com.github.panpf.zoomimage.util.times
import kotlin.math.max

data class ReadMode(
    val direction: Direction = Direction.Both,
    val decider: Decider = Decider.Default
) {

    fun accept(srcSize: IntSizeCompat, dstSize: IntSizeCompat): Boolean {
        val directionMatched = when (direction) {
            Direction.OnlyHorizontal -> srcSize.width > srcSize.height
            Direction.OnlyVertical -> srcSize.width < srcSize.height
            else -> true
        }
        return if (directionMatched) decider.should(srcSize = srcSize, dstSize = dstSize) else false
    }

    fun computeTransform(
        containerSize: IntSizeCompat,
        contentSize: IntSizeCompat,
        baseTransform: TransformCompat,
    ): TransformCompat {
        val widthScale = containerSize.width / contentSize.width.toFloat()
        val heightScale = containerSize.height / contentSize.height.toFloat()
        val fillScale = max(widthScale, heightScale)
        val addScale = fillScale / baseTransform.scaleX
        val scaleX = baseTransform.scaleX * addScale
        val scaleY = baseTransform.scaleY * addScale
        val translateX = if (baseTransform.offset.x < 0)
            baseTransform.offset.x * addScale else 0f
        val translateY = if (baseTransform.offset.y < 0)
            baseTransform.offset.y * addScale else 0f
        return TransformCompat(
            scale = ScaleFactorCompat(scaleX = scaleX, scaleY = scaleY),
            offset = OffsetCompat(x = translateX, y = translateY)
        )
    }

    companion object {
        val Default = ReadMode(direction = Direction.Both, decider = Decider.Default)
    }

    enum class Direction {
        Both, OnlyHorizontal, OnlyVertical
    }

    interface Decider {

        fun should(srcSize: IntSizeCompat, dstSize: IntSizeCompat): Boolean

        companion object {
            val Default = LongImageDecider()
        }
    }

    class LongImageDecider(
        val sameDirectionMultiple: Float = 2.5f,
        val notSameDirectionMultiple: Float = 5.0f,
    ) : Decider {

        override fun should(srcSize: IntSizeCompat, dstSize: IntSizeCompat): Boolean {
            val fillScale = max(
                dstSize.width / srcSize.width.toFloat(), dstSize.height / srcSize.height.toFloat()
            )
            val filledSrcSize = srcSize.times(ScaleFactorCompat(fillScale))
            val maxScaleMultiple = max(
                filledSrcSize.width / dstSize.width.toFloat(),
                filledSrcSize.height / dstSize.height.toFloat()
            )
            val sameDirection = isSameDirection(srcSize = srcSize, dstSize = dstSize)
            val minMultiple = if (sameDirection) sameDirectionMultiple else notSameDirectionMultiple
            return maxScaleMultiple.format(1) >= minMultiple.format(1)
        }

        override fun toString(): String {
            return "LongImageDecider(same=$sameDirectionMultiple,notSame=$notSameDirectionMultiple)"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
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
    }
}