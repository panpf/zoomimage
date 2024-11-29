package com.github.panpf.zoomimage.sample.ui.examples

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.sketch.SingletonSketch
import com.github.panpf.sketch.asPainter
import com.github.panpf.sketch.painter.AnimatablePainter
import com.github.panpf.sketch.painter.startWithLifecycle
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.sketch.request.execute
import com.github.panpf.zoomimage.ZoomImage
import com.github.panpf.zoomimage.compose.rememberZoomState
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.ui.components.PageState
import com.github.panpf.zoomimage.sample.ui.model.Photo
import com.github.panpf.zoomimage.sample.ui.test.sketchImageUriToZoomImageImageSource
import com.github.panpf.zoomimage.sketch.SketchTileImageCache
import com.github.panpf.zoomimage.subsampling.ImageInfo

@Composable
fun BasicZoomImageSample(
    photo: Photo,
    photoPaletteState: MutableState<PhotoPalette>,
    pageSelected: Boolean,
) {
    BaseZoomImageSample(
        photo = photo,
        photoPaletteState = photoPaletteState,
        createZoomState = { rememberZoomState() },
        pageSelected = pageSelected,
    ) { contentScale, alignment, zoomState, scrollBar, onLongClick, onTapClick ->
        val context = LocalPlatformContext.current
        val sketch = SingletonSketch.get(context)
        LaunchedEffect(Unit) {
            zoomState.subsampling.tileImageCache = SketchTileImageCache(sketch)
        }

        var pageState by remember { mutableStateOf<PageState?>(null) }
        var imagePainter: Painter? by remember { mutableStateOf(null) }
        LaunchedEffect(photo) {
            pageState = PageState.Loading
            val imageResult = ImageRequest(context, photo.originalUrl) {
                getPlatformSketchZoomAsyncImageSampleImageOptions()
            }.execute()
            pageState = if (imageResult is ImageResult.Success) {
                null
            } else {
                PageState.Error()
            }
            imagePainter = imageResult.image?.asPainter()

            val imageSource = sketchImageUriToZoomImageImageSource(
                sketch = sketch,
                imageUri = photo.originalUrl,
                http2ByteArray = false
            )
            val imageInfo = if (imageResult is ImageResult.Success) {
                ImageInfo(
                    imageResult.imageInfo.width,
                    imageResult.imageInfo.height,
                    imageResult.imageInfo.mimeType
                )
            } else {
                null
            }
            zoomState.setSubsamplingImage(imageSource, imageInfo)
        }

        val imagePainter1 = imagePainter
        if (imagePainter1 is AnimatablePainter) {
            imagePainter1.startWithLifecycle()
        }
        if (imagePainter1 != null) {
            ZoomImage(
                painter = imagePainter1,
                contentDescription = "view image",
                contentScale = contentScale,
                alignment = alignment,
                modifier = Modifier.fillMaxSize(),
                zoomState = zoomState,
                scrollBar = scrollBar,
                onLongPress = {
                    onLongClick.invoke()
                },
                onTap = {
                    onTapClick.invoke(it)
                }
            )
        }

        PageState(pageState = pageState)
    }
}