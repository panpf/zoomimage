package com.github.panpf.zoomimage.sample.ui.test

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.fetch.Fetcher
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.RequestContext
import com.github.panpf.sketch.util.Size
import com.github.panpf.zoomimage.SketchZoomAsyncImage
import com.github.panpf.zoomimage.compose.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.sample.ui.base.ToolbarScaffold
import com.github.panpf.zoomimage.sample.ui.components.HorizontalTabPager
import com.github.panpf.zoomimage.sample.ui.components.PagerItem
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

@Composable
fun ImageSourceTestScreen() {
    ToolbarScaffold("ImageSource") {
        val context = LocalPlatformContext.current
        var imageSourceTestItems by remember { mutableStateOf<List<Pair<String, String>>?>(null) }
        LaunchedEffect(Unit) {
            imageSourceTestItems = getImageSourceTestItems(context)
        }
        val imageSourceTestItems1 = imageSourceTestItems
        if (imageSourceTestItems1 != null) {
            val pagerItems = remember {
                imageSourceTestItems1.map { pair ->
                    PagerItem(
                        data = pair,
                        titleFactory = { data ->
                            data.first
                        },
                        contentFactory = { data, _, _ ->
                            SketchZoomAsyncImage(
                                uri = data.second,
                                contentDescription = "view image",
                                modifier = Modifier.fillMaxSize(),
                                scrollBar = ScrollBarSpec.Medium.copy(
                                    windowInsets = WindowInsets.navigationBars
                                ),
                            )
                        }
                    )
                }.toTypedArray()
            }
            Box(Modifier.fillMaxSize()) {
                HorizontalTabPager(pagerItems = pagerItems)
            }
        }
    }
}