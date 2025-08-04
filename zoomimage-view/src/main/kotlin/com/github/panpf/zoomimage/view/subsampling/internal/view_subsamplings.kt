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

package com.github.panpf.zoomimage.view.subsampling.internal

import android.graphics.Canvas
import android.graphics.Matrix
import com.github.panpf.zoomimage.util.isNotEmpty
import com.github.panpf.zoomimage.view.util.applyOriginToThumbnailScale
import com.github.panpf.zoomimage.view.util.applyTransform
import com.github.panpf.zoomimage.view.zoom.ZoomableEngine

fun Canvas.withZooming(
    zoomableEngine: ZoomableEngine,
    cacheMatrix: Matrix,
    firstScaleByContentSize: Boolean = false,
    block: Canvas.() -> Unit
) {
    val checkpoint: Int = save()

    val transform = zoomableEngine.transformState.value
    val containerSize = zoomableEngine.containerSizeState.value
    if (containerSize.isNotEmpty()) {
        val transformMatrix = cacheMatrix.applyTransform(transform, containerSize)
        concat(/* matrix = */ transformMatrix)

        val contentSize = zoomableEngine.contentSizeState.value
        val contentOriginSize = zoomableEngine.contentOriginSizeState.value
        if (firstScaleByContentSize && contentSize.isNotEmpty()) {
            val originToThumbnailScaleMatrix = cacheMatrix.applyOriginToThumbnailScale(
                originImageSize = contentOriginSize.takeIf { it.isNotEmpty() } ?: contentSize,
                thumbnailImageSize = contentSize
            )
            concat(/* matrix = */ originToThumbnailScaleMatrix)
        }
    }

    try {
        block()
    } finally {
        restoreToCount(checkpoint)
    }
}