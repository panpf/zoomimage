package com.github.panpf.zoom.sample

import androidx.multidex.MultiDexApplication
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.SketchFactory
import com.github.panpf.sketch.util.Logger
import com.github.panpf.sketch.util.Logger.Level.DEBUG
import com.github.panpf.sketch.util.Logger.Level.INFO
import com.tencent.mmkv.MMKV

class MyApplication : MultiDexApplication(), SketchFactory {

    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
    }

    override fun createSketch(): Sketch {
        return Sketch.Builder(this)
            .logger(Logger(if (BuildConfig.DEBUG) DEBUG else INFO))
            .build()
    }
}