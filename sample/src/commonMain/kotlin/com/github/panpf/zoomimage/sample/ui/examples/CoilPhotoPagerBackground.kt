package com.github.panpf.zoomimage.sample.ui.examples

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.BitmapImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.github.panpf.sketch.util.ioCoroutineDispatcher
import com.github.panpf.zoomimage.sample.image.CoilBlurInterceptor
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.image.asSketchImage
import com.github.panpf.zoomimage.sample.image.toSimplePalette
import com.github.panpf.zoomimage.sample.ui.util.windowSize
import com.github.panpf.zoomimage.sample.util.sketchUri2CoilModel
import com.kmpalette.palette.graphics.Palette
import kotlinx.coroutines.launch

@Composable
fun CoilPagerBackground(
    sketchImageUri: String,
    photoPaletteState: MutableState<PhotoPalette>,
) {
    val windowsSize = windowSize()
    val context = LocalPlatformContext.current
    val coroutineScope = rememberCoroutineScope()
    val request = remember(sketchImageUri) {
        ImageRequest.Builder(context).apply {
            data(sketchUri2CoilModel(context, sketchImageUri))
            size(
                width = windowsSize.width / 4,
                height = windowsSize.height / 4,
            )
            // TODO Since the memory cache feature of coil will cause pollution of the memory cache, it cannot be solved for the time being, and modifying precision is useless, can only be temporarily banned.
            memoryCachePolicy(CachePolicy.DISABLED)
            extras[CoilBlurInterceptor.blurKey] = true
            crossfade(durationMillis = 400)
            listener(onSuccess = { _, result ->
                coroutineScope.launch(ioCoroutineDispatcher()) {
                    val image = (result.image as BitmapImage).asSketchImage()
                    val palette = try {
                        Palette.Builder(image).generate()
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        null
                    }
                    if (palette != null) {
                        photoPaletteState.value = PhotoPalette(palette.toSimplePalette())
                    }
                }
            })
        }.build()
    }
    coil3.compose.AsyncImage(
        model = request,
        contentDescription = "Background",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
}