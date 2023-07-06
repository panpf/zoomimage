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

import com.github.panpf.zoomimage.core.SizeCompat
import com.github.panpf.zoomimage.core.internal.format

// todo 添加图片方向，例如为了不影响 Pager 横向滑动的连贯性，只有竖的长图才允许使用阅读模式，分为 Horizontal，vertical，both
data class ReadMode(val enabled: Boolean, val decider: ReadModeDecider) {

    companion object {
        val Default = ReadMode(enabled = false, decider = ReadModeDecider.Default)
    }
}

interface ReadModeDecider {
    fun should(srcSize: SizeCompat, dstSize: SizeCompat): Boolean

    companion object {
        val Default = LongImageReadModeDecider()
    }
}

class LongImageReadModeDecider(
    val sameDirectionMultiple: Float = 2.5f,
    val notSameDirectionMultiple: Float = 5.0f,
) : ReadModeDecider {

    override fun should(srcSize: SizeCompat, dstSize: SizeCompat): Boolean =
        isLongImage(srcSize = srcSize, dstSize = dstSize)

    /**
     * Determine whether it is a long image given the image size and target size
     *
     * If the directions of image and target are the same, then the aspect ratio of
     * the two is considered as a long image when the aspect ratio reaches [sameDirectionMultiple] times,
     * otherwise it is considered as a long image when it reaches [notSameDirectionMultiple] times
     */
    private fun isLongImage(
        srcSize: SizeCompat, dstSize: SizeCompat
    ): Boolean {
        val srcAspectRatio = srcSize.width.toFloat().div(srcSize.height).format(2)
        val dstAspectRatio = dstSize.width.toFloat().div(dstSize.height).format(2)
        val sameDirection = srcAspectRatio == 1.0f
                || dstAspectRatio == 1.0f
                || (srcAspectRatio > 1.0f && dstAspectRatio > 1.0f)
                || (srcAspectRatio < 1.0f && dstAspectRatio < 1.0f)
        val ratioMultiple = if (sameDirection) sameDirectionMultiple else notSameDirectionMultiple
        return if (ratioMultiple > 0) {
            val maxAspectRatio = dstAspectRatio.coerceAtLeast(srcAspectRatio)
            val minAspectRatio = dstAspectRatio.coerceAtMost(srcAspectRatio)
            maxAspectRatio >= (minAspectRatio * ratioMultiple)
        } else {
            false
        }
    }

    override fun toString(): String {
        return "LongImageReadModeDecider(sameDirectionMultiple=$sameDirectionMultiple, notSameDirectionMultiple=$notSameDirectionMultiple)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as LongImageReadModeDecider
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