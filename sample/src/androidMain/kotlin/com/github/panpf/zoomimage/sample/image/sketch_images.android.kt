package com.github.panpf.zoomimage.sample.image

import com.github.panpf.sketch.DrawableImage
import com.github.panpf.sketch.Image
import com.github.panpf.sketch.PainterImage
import com.github.panpf.sketch.drawable.ResizeDrawable
import com.github.panpf.sketch.painter.ResizePainter
import com.github.panpf.sketch.util.Size
import com.github.panpf.sketch.util.toSketchSize
import com.github.panpf.zoomimage.sample.ui.util.round

actual val Image.realSize: Size
    get() = if (this is PainterImage) {
        val painter = painter
        if (painter is ResizePainter) {
            painter.painter.intrinsicSize.round().toSketchSize()
        } else {
            Size(width, height)
        }
    } else if (this is DrawableImage) {
        val drawable = drawable
        if (drawable is ResizeDrawable) {
            drawable.drawable!!.let {
                Size(it.intrinsicWidth, it.intrinsicHeight)
            }
        } else {
            Size(width, height)
        }
    } else {
        Size(width, height)
    }