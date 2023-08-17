package com.github.panpf.zoomimage.view.internal

import android.graphics.Matrix
import android.widget.ImageView.ScaleType
import com.github.panpf.zoomimage.util.AlignmentCompat
import com.github.panpf.zoomimage.util.ContentScaleCompat
import com.github.panpf.zoomimage.util.TransformCompat

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

fun Matrix.applyTransform(transform: TransformCompat): Matrix {
    reset()
    postRotate(transform.rotation, transform.rotationOriginX, transform.rotationOriginY)
    postScale(transform.scale.scaleX, transform.scale.scaleY)
    postTranslate(transform.offset.x, transform.offset.y)
    return this
}