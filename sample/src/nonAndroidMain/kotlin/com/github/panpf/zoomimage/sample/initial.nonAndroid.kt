package com.github.panpf.zoomimage.sample

import coil3.PlatformContext as CoilPlatformContext
import coil3.SingletonImageLoader
import com.github.panpf.sketch.Sketch
import com.github.panpf.zoomimage.sample.util.ignoreFirst
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform

@OptIn(DelicateCoroutinesApi::class)
fun cleanImageLoaderMemoryCache() {
    GlobalScope.launch {
        val coilContext = CoilPlatformContext.INSTANCE
        val appSettings: AppSettings = KoinPlatform.getKoin().get()
        val sketch: Sketch = KoinPlatform.getKoin().get()
        appSettings.composeImageLoader.ignoreFirst().collect { newImageLoader ->
            if (newImageLoader != "Sketch" && newImageLoader != "Basic") {
                sketch.memoryCache.clear()
            }
            if (newImageLoader != "Coil") {
                SingletonImageLoader.get(coilContext).memoryCache?.clear()
            }
        }
    }
}