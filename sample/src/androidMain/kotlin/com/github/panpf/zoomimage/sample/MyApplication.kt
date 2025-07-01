package com.github.panpf.zoomimage.sample

import android.annotation.SuppressLint
import android.app.Application
import coil3.ImageLoader
import coil3.SingletonImageLoader
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.SingletonSketch
import com.github.panpf.sketch.Sketch

class MyApplication : Application(), SingletonSketch.Factory, SingletonImageLoader.Factory {

    @SuppressLint("VisibleForTests")
    override fun onCreate() {
        super.onCreate()
        initialApp(this@MyApplication)
    }

    override fun createSketch(context: PlatformContext): Sketch {
        return newSketch(context)
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return newCoil(context)
    }
}