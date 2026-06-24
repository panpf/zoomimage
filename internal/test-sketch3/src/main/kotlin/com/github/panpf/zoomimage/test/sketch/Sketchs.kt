package com.github.panpf.zoomimage.test.sketch

import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.SketchSingleton
import com.github.panpf.sketch.util.Logger

object Sketchs {

    init {
        SketchSingleton.setSketch {
            val context = InstrumentationRegistry.getInstrumentation().context
            Sketch.Builder(context).apply {
                logger(Logger(level = Logger.Level.DEBUG))
            }.build()
        }
    }

    fun sketch(): Sketch {
        val context = InstrumentationRegistry.getInstrumentation().context
        return SketchSingleton.sketch(context)
    }
}