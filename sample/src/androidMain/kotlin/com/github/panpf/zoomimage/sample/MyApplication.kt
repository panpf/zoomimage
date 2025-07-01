package com.github.panpf.zoomimage.sample

import android.app.Application
import coil3.ImageLoader
import coil3.SingletonImageLoader
import com.github.panpf.sketch.PlatformContext

class MyApplication : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        initialApp(this@MyApplication)
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return newCoil(context)
    }
}