package com.github.panpf.zoomimage.test.sketch

import com.github.panpf.sketch.SingletonSketch
import com.github.panpf.sketch.Sketch

object Sketchs {

    init {
        SingletonSketch.setSafe {
            Sketch.Builder(platformContext).build()
        }
    }

    fun sketch(): Sketch {
        return SingletonSketch.get(platformContext)
    }
}