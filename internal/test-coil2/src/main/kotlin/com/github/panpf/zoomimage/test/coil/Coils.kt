package com.github.panpf.zoomimage.test.coil

import androidx.test.platform.app.InstrumentationRegistry
import coil.Coil
import coil.ImageLoader
import coil.util.DebugLogger

object Coils {

    init {
        Coil.setImageLoader {
            val context = InstrumentationRegistry.getInstrumentation().context
            ImageLoader.Builder(context).apply {
                logger(DebugLogger())
            }.build()
        }
    }

    fun imageLoader(): ImageLoader {
        val context = InstrumentationRegistry.getInstrumentation().context
        return Coil.imageLoader(context)
    }
}