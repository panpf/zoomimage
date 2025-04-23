package com.github.panpf.zoomimage.sample.image

import com.github.panpf.sketch.request.ImageData
import com.github.panpf.sketch.request.RequestInterceptor
import com.github.panpf.zoomimage.sample.appSettings
import kotlinx.coroutines.delay

data class DelayedLoadRequestInterceptor(val delay: Long) : RequestInterceptor {

    override val key: String? = null

    override val sortWeight: Int = 95   // After MemoryCacheRequestInterceptor

    override suspend fun intercept(chain: RequestInterceptor.Chain): Result<ImageData> {
        val result = chain.proceed(chain.request)
        if (chain.sketch.context.appSettings.delayImageLoadEnabled.value) {
            delay(delay)
        }
        return result
    }
}