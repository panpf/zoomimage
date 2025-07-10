package com.github.panpf.zoomimage.sample.ui.test

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.GlideZoomAsyncImage
import com.github.panpf.zoomimage.compose.glide.ExperimentalGlideComposeApi
import com.github.panpf.zoomimage.sample.ui.base.BaseScreen
import com.github.panpf.zoomimage.sample.ui.base.ToolbarScaffold

class TempAndroidTestScreen : BaseScreen() {

    @Composable
    override fun DrawContent() {
        ToolbarScaffold("Temp (Android)") {
            GlideThumbnailSample()
        }
    }

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    fun GlideThumbnailSample() {
        val context = LocalContext.current
        GlideZoomAsyncImage(
            model = ResourceImages.hugeChina.uri,
            modifier = Modifier.fillMaxSize(),
            contentDescription = null,
            requestBuilderTransform = {
                it.thumbnail(
                    Glide.with(context)
                        .load(ResourceImages.hugeChinaThumbnail.uri)
                        .override(120, 120)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                )
                    .priority(Priority.IMMEDIATE)
                    .encodeQuality(100)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .format(DecodeFormat.PREFER_ARGB_8888)
            }
        )
    }
}