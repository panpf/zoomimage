package com.github.panpf.zoomimage.sample.image

import com.github.panpf.sketch.request.ImageData
import com.github.panpf.sketch.request.RequestInterceptor
import com.github.panpf.zoomimage.sample.AppSettings
import kotlinx.coroutines.delay
import org.koin.mp.KoinPlatform

data class DelayedLoadRequestInterceptor(val delay: Long) : RequestInterceptor {

    override val key: String? = null

    override val sortWeight: Int = 95   // After MemoryCacheRequestInterceptor

    override suspend fun intercept(chain: RequestInterceptor.Chain): Result<ImageData> {
        val result = chain.proceed(chain.request)
        val appSettings: AppSettings = KoinPlatform.getKoin().get()
        if (appSettings.delayImageLoadEnabled.value) {
            delay(delay)
        }
        return result
    }
}