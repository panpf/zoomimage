package com.github.panpf.zoomimage.sample.ui.test

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.crossfade
import coil3.size.Precision
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import com.github.panpf.zoomimage.rememberCoilZoomState
import com.github.panpf.zoomimage.sample.ui.base.BaseScreen
import com.github.panpf.zoomimage.sample.ui.base.ToolbarScaffold
import com.github.panpf.zoomimage.util.Logger

class CoilBigStartCrossfadeTestScreen : BaseScreen() {

    @Composable
    override fun DrawContent() {
        ToolbarScaffold("CoilBigStartCrossfade") {
            val context = LocalPlatformContext.current
            var bigImageMemoryCacheKey: MemoryCache.Key? by remember { mutableStateOf(null) }
            LaunchedEffect(Unit) {
                val imageLoader = SingletonImageLoader.get(context)
                val request = ImageRequest.Builder(context)
                    .data(ResourceImages.dog.uri)
                    .size(1100, 733)
                    .precision(Precision.EXACT)
                    .build()
                val result = imageLoader.execute(request)
                if (result is SuccessResult) {
                    bigImageMemoryCacheKey = result.memoryCacheKey
                }
            }

            val bigImageMemoryCacheKey1 = bigImageMemoryCacheKey
            if (bigImageMemoryCacheKey1 != null) {
                CoilZoomAsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = ImageRequest.Builder(context)
                        .data(ResourceImages.dog.uri)
                        .placeholderMemoryCacheKey(bigImageMemoryCacheKey1)
                        .memoryCachePolicy(CachePolicy.DISABLED)
                        .size(1100 / 2, 733 / 2)
                        .precision(Precision.EXACT)
                        .crossfade(durationMillis = 300)
                        .build(),
                    zoomState = rememberCoilZoomState(logLevel = Logger.Level.Debug),
                    contentDescription = null,
                )
            }
        }
    }
}