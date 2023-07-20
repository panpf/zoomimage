package com.github.panpf.zoomimage.sketch.internal

import android.graphics.Bitmap
import com.github.panpf.sketch.Sketch
import com.github.panpf.zoomimage.subsampling.TileBitmapPool

class SketchTileBitmapPool(private val sketch: Sketch) : TileBitmapPool {

    override fun put(bitmap: Bitmap): Boolean {
        return sketch.bitmapPool.put(bitmap, "SubsamplingImageView")
    }

    override fun get(width: Int, height: Int, config: Bitmap.Config): Bitmap? {
        return sketch.bitmapPool.get(width, height, config)
    }
}