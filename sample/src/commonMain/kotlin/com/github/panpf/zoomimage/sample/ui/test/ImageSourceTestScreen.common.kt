package com.github.panpf.zoomimage.sample.ui.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.SingletonSketch
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.asPainter
import com.github.panpf.sketch.cache.CachePolicy.DISABLED
import com.github.panpf.sketch.fetch.Fetcher
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.sketch.request.RequestContext
import com.github.panpf.sketch.request.execute
import com.github.panpf.sketch.util.Size
import com.github.panpf.zoomimage.ZoomImage
import com.github.panpf.zoomimage.compose.rememberZoomState
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.ui.base.BaseScreen
import com.github.panpf.zoomimage.sample.ui.base.ToolbarScaffold
import com.github.panpf.zoomimage.sample.ui.components.HorizontalTabPager
import com.github.panpf.zoomimage.sample.ui.components.PageState
import com.github.panpf.zoomimage.sample.ui.components.PageState.Error
import com.github.panpf.zoomimage.sample.ui.components.PageState.Loading
import com.github.panpf.zoomimage.sample.ui.components.PagerItem
import com.github.panpf.zoomimage.sample.ui.examples.BaseZoomImageSample
import com.github.panpf.zoomimage.sample.ui.model.Photo
import com.github.panpf.zoomimage.sketch.SketchTileImageCache
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource

expect suspend fun getImageSourceTestItems(context: PlatformContext): List<Pair<String, String>>

suspend fun sketchImageUriToZoomImageImageSource(
    sketch: Sketch,
    imageUri: String,
    http2ByteArray: Boolean = false
): ImageSource.Factory? {
    val request = ImageRequest(sketch.context, imageUri)
    val requestContext = RequestContext(sketch, request, Size.Empty)
    val fetcher = sketch.components.newFetcherOrThrow(requestContext)
    return sketchFetcherToZoomImageImageSource(sketch.context, fetcher, http2ByteArray)
}

expect suspend fun sketchFetcherToZoomImageImageSource(
    context: PlatformContext,
    fetcher: Fetcher,
    http2ByteArray: Boolean = false
): ImageSource.Factory?

class ImageSourceTestScreen : BaseScreen() {

    @Composable
    override fun DrawContent() {
        val context = LocalPlatformContext.current
        var imageSourceTestItems by remember { mutableStateOf<List<Pair<String, String>>?>(null) }
        LaunchedEffect(Unit) {
            imageSourceTestItems = getImageSourceTestItems(context)
        }
        val imageSourceTestItems1 = imageSourceTestItems
        if (imageSourceTestItems1 != null) {
            ToolbarScaffold("ImageSource", ignoreNavigationBarInsets = true) {
                val colorScheme = MaterialTheme.colorScheme
                val pagerItems = remember {
                    imageSourceTestItems1.map { pair ->
                        PagerItem(
                            data = pair,
                            titleFactory = { data ->
                                data.first
                            },
                            contentFactory = { data, _, pageSelected ->
                                val photoPaletteState =
                                    remember { mutableStateOf(PhotoPalette(colorScheme)) }
                                ImageSourceSample(data.second, photoPaletteState, pageSelected)
                            }
                        )
                    }.toTypedArray()
                }
                Box(Modifier.fillMaxSize().background(Color.Black)) {
                    HorizontalTabPager(pagerItems = pagerItems)
                }
            }
        }
    }

    @Composable
    fun ImageSourceSample(
        sketchImageUri: String,
        photoPaletteState: MutableState<PhotoPalette>,
        pageSelected: Boolean,
    ) {
        BaseZoomImageSample(
            photo = Photo(sketchImageUri),
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
            LaunchedEffect(sketchImageUri) {
                pageState = Loading
                val imageRequest = ImageRequest(context, sketchImageUri) {
                    memoryCachePolicy(DISABLED)
                }
                val imageResult = imageRequest.execute()
                pageState = if (imageResult is ImageResult.Success) {
                    null
                } else {
                    Error()
                }
                imagePainter = imageResult.image?.asPainter()

                val imageSource = sketchImageUriToZoomImageImageSource(
                    sketch = sketch,
                    imageUri = sketchImageUri,
                    http2ByteArray = true
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
}