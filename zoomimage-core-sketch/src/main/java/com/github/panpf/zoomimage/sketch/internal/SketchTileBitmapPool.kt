package com.github.panpf.zoomimage.sketch.internal

import android.graphics.Bitmap
import com.github.panpf.sketch.Sketch
import com.github.panpf.zoomimage.subsampling.TileBitmapPool

class SketchTileBitmapPool constructor(
    private val sketch: Sketch,
    private val caller: String
) : TileBitmapPool {

    override fun put(bitmap: Bitmap): Boolean {
        return sketch.bitmapPool.put(bitmap, caller)
    }

    override fun get(width: Int, height: Int, config: Bitmap.Config): Bitmap? {
        return sketch.bitmapPool.get(width, height, config)
    }
}