package com.github.panpf.zoomimage.sample.image

import com.github.panpf.sketch.cache.internal.MemoryCacheInterceptor
import com.github.panpf.sketch.request.ImageData
import com.github.panpf.sketch.request.Interceptor
import com.github.panpf.zoomimage.sample.AppSettings
import kotlinx.coroutines.delay
import org.koin.mp.KoinPlatform

data class DelayedLoadInterceptor(val delay: Long) : Interceptor {

    override val key: String? = null
    override val sortWeight: Int = MemoryCacheInterceptor.SORT_WEIGHT + 1

    override suspend fun intercept(chain: Interceptor.Chain): Result<ImageData> {
        val result = chain.proceed(chain.request)
        val appSettings: AppSettings = KoinPlatform.getKoin().get()
        if (appSettings.delayImageLoadEnabled.value) {
            delay(delay)
        }
        return result
    }
}