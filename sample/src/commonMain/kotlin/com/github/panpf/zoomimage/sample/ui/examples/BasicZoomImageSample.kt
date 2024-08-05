package com.github.panpf.zoomimage.sample.ui.examples

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.sketch.SingletonSketch
import com.github.panpf.sketch.painter.AnimatablePainter
import com.github.panpf.sketch.painter.asPainter
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.sketch.request.execute
import com.github.panpf.zoomimage.ZoomImage
import com.github.panpf.zoomimage.compose.rememberZoomState
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.ui.components.MyPageState
import com.github.panpf.zoomimage.sample.ui.components.PageState
import com.github.panpf.zoomimage.sample.ui.model.Photo
import com.github.panpf.zoomimage.sample.ui.test.sketchImageUriToZoomImageImageSource
import com.github.panpf.zoomimage.sketch.SketchTileBitmapCache

@Composable
fun BasicZoomImageSample(photo: Photo, photoPaletteState: MutableState<PhotoPalette>) {
    BaseZoomImageSample(
        photo = photo,
        photoPaletteState = photoPaletteState,
        createZoomState = { rememberZoomState() }
    ) { contentScale, alignment, zoomState, scrollBar, onLongClick ->
        val context = LocalPlatformContext.current
        val sketch = SingletonSketch.get(context)
        LaunchedEffect(Unit) {
            zoomState.subsampling.tileBitmapCache = SketchTileBitmapCache(sketch)
        }

        var myLoadState by remember { mutableStateOf<MyPageState>(MyPageState.None) }
        var imagePainter: Painter? by remember { mutableStateOf(null) }
        LaunchedEffect(photo) {
            myLoadState = MyPageState.Loading
            val imageResult = ImageRequest(context, photo.originalUrl).execute()
            myLoadState = if (imageResult is ImageResult.Success) {
                MyPageState.None
            } else {
                MyPageState.Error()
            }
            imagePainter = imageResult.image?.asPainter()

            val imageSource = sketchImageUriToZoomImageImageSource(
                sketch = sketch,
                imageUri = photo.originalUrl,
                http2ByteArray = false
            )
            zoomState.subsampling.setImageSource(imageSource)
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
                }
            )
        }

        PageState(state = myLoadState)
    }
}

// TODO The new version of Sketch will include this extension function
@Composable
fun AnimatablePainter.startWithLifecycle() {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val animatablePainter =
        remember(this) { this } // SkiaAnimatedImagePainter needs to trigger onRemembered
    DisposableEffect(animatablePainter) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                animatablePainter.start()
            } else if (event == Lifecycle.Event.ON_STOP) {
                animatablePainter.stop()
            }
        }
        // if the LifecycleOwner is in [State.STARTED] state, the given observer * will receive [Event.ON_CREATE], [Event.ON_START] events.
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}