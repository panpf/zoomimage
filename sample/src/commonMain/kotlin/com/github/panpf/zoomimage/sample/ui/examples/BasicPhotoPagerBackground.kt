package com.github.panpf.zoomimage.sample.ui.examples

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.sketch.cache.CachePolicy.DISABLED
import com.github.panpf.sketch.rememberAsyncImagePainter
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.sketch.request.disallowAnimatedImage
import com.github.panpf.sketch.resize.Precision.SMALLER_SIZE
import com.github.panpf.sketch.transform.BlurTransformation
import com.github.panpf.zoomimage.sample.image.PaletteDecodeInterceptor
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.image.simplePalette
import com.github.panpf.zoomimage.sample.ui.util.isEmpty

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
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    Box(
        modifier = Modifier.fillMaxSize().onSizeChanged {
            imageSize = IntSize(it.width / 4, it.height / 4)
        }
    ) {
        val context = LocalPlatformContext.current
        val request by remember(sketchImageUri) {
            derivedStateOf {
                if (imageSize.isEmpty()) {
                    null
                } else {
                    ImageRequest(context, sketchImageUri) {
                        resize(
                            width = imageSize.width,
                            height = imageSize.height,
                            precision = SMALLER_SIZE
                        )
                        addTransformations(
                            BlurTransformation(radius = 20, maskColor = 0x63000000)
                        )
                        memoryCachePolicy(DISABLED)
                        resultCachePolicy(DISABLED)
                        disallowAnimatedImage()
                        crossfade(alwaysUse = true, durationMillis = 400)
                        resizeOnDraw()
                        components {
                            addDecodeInterceptor(PaletteDecodeInterceptor())
                        }
                    }
                }
            }
        }
        val request1 = request
        if (request1 != null) {
            Image(
                painter = rememberAsyncImagePainter(
                    request = request1,
                    state = imageState,
                    contentScale = ContentScale.Crop
                ),
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}