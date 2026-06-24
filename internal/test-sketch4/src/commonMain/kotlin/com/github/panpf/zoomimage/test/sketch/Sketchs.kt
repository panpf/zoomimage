package com.github.panpf.zoomimage.test.sketch

import com.github.panpf.sketch.SingletonSketch
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.util.Logger

object Sketchs {

    init {
        SingletonSketch.setSafe {
            Sketch.Builder(platformContext).apply {
                logger(Logger(level = Logger.Level.Debug))
            }.build()
        }
    }

    fun sketch(): Sketch {
        return SingletonSketch.get(platformContext)
    }
}