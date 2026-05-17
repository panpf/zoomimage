package com.github.panpf.zoomimage.sample.ui.examples

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.github.panpf.sketch.cache.CachePolicy.DISABLED
import com.github.panpf.sketch.rememberAsyncImagePainter
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ComposableImageRequest
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.sketch.request.disallowAnimatedImage
import com.github.panpf.sketch.resize.Precision.SMALLER_SIZE
import com.github.panpf.sketch.transform.BlurTransformation
import com.github.panpf.sketch.util.toSketchSize
import com.github.panpf.zoomimage.sample.image.PaletteDecodeInterceptor
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.image.simplePalette
import com.github.panpf.zoomimage.sample.ui.util.windowSize

@Composable
fun BasicPagerBackground(
    sketchImageUri: String,
    photoPaletteState: MutableState<PhotoPalette>,
) {
    val colorScheme = MaterialTheme.colorScheme
    val imageState = rememberAsyncImageState()
    LaunchedEffect(Unit) {
        snapshotFlow { imageState.result }.collect {
            if (it is ImageResult.Success) {
                photoPaletteState.value =
                    PhotoPalette(it.simplePalette, colorScheme = colorScheme)
            }
        }
    }
    // Cache the image size to prevent reloading the image when the window size changes
    val windowsSize = windowSize()
    val imageSize = remember { (windowsSize / 4).toSketchSize() }
    val request = ComposableImageRequest(sketchImageUri) {
        resize(size = imageSize, precision = SMALLER_SIZE)
        addTransformations(BlurTransformation(radius = 20, maskColor = 0x63000000))
        memoryCachePolicy(DISABLED)
        resultCachePolicy(DISABLED)
        disallowAnimatedImage()
        crossfade(alwaysUse = true, durationMillis = 400)
        resizeOnDraw()
        components {
            addDecodeInterceptor(PaletteDecodeInterceptor())
        }
    }
    Image(
        painter = rememberAsyncImagePainter(
            request = request,
            state = imageState,
            contentScale = ContentScale.Crop
        ),
        contentDescription = "Background",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
}