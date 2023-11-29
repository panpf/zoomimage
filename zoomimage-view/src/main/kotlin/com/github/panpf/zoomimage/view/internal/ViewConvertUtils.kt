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

package com.github.panpf.zoomimage.view.internal

import android.graphics.Matrix
import android.widget.ImageView.ScaleType
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContentScaleCompat

fun ScaleType.toContentScale(): ContentScaleCompat {
    return when (this) {
        ScaleType.MATRIX -> ContentScaleCompat.None
        ScaleType.FIT_XY -> ContentScaleCompat.FillBounds
        ScaleType.FIT_START -> ContentScaleCompat.Fit
        ScaleType.FIT_CENTER -> ContentScaleCompat.Fit
        ScaleType.FIT_END -> ContentScaleCompat.Fit
        ScaleType.CENTER -> ContentScaleCompat.None
        ScaleType.CENTER_CROP -> ContentScaleCompat.Crop
        ScaleType.CENTER_INSIDE -> ContentScaleCompat.Inside
    }
}

fun ScaleType.toAlignment(): AlignmentCompat {
    return when (this) {
        ScaleType.MATRIX -> AlignmentCompat.TopStart
        ScaleType.FIT_XY -> AlignmentCompat.TopStart
        ScaleType.FIT_START -> AlignmentCompat.TopStart
        ScaleType.FIT_CENTER -> AlignmentCompat.Center
        ScaleType.FIT_END -> AlignmentCompat.BottomEnd
        ScaleType.CENTER -> AlignmentCompat.Center
        ScaleType.CENTER_CROP -> AlignmentCompat.Center
        ScaleType.CENTER_INSIDE -> AlignmentCompat.Center
    }
}

fun Matrix.applyTransform(transform: TransformCompat, containerSize: IntSizeCompat): Matrix {
    reset()
    postRotate(
        /* degrees = */ transform.rotation,
        /* px = */ transform.rotationOriginX * containerSize.width,
        /* py = */ transform.rotationOriginY * containerSize.height
    )
    postScale(transform.scale.scaleX, transform.scale.scaleY)
    postTranslate(transform.offset.x, transform.offset.y)
    return this
}