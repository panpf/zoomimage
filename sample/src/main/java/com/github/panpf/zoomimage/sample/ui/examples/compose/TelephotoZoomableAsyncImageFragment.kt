package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest
import com.github.panpf.zoomimage.sample.SampleImages
import com.github.panpf.zoomimage.sample.ui.base.compose.AppBarFragment
import com.github.panpf.zoomimage.sample.ui.common.compose.HorizontalTabPager
import com.github.panpf.zoomimage.sample.ui.common.compose.PagerItem
import com.github.panpf.zoomimage.sample.util.sketchUri2CoilModel
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState

class TelephotoZoomableAsyncImagePagerFragment : AppBarFragment() {

    override fun getTitle(): String {
        return "ZoomableAsyncImage"
    }

    override fun getSubtitle(): String {
        return "Telephoto"
    }

    @Composable
    override fun DrawContent() {
        val context = LocalContext.current
        val items = remember {
            SampleImages.FETCHERS.map { sampleImage ->
                PagerItem(
                    data = sampleImage,
                    titleFactory = { it.name },
                    contentFactory = { _, _ ->
                        ZoomableAsyncImageFullSample(sketchUri2CoilModel(context, sampleImage.uri))
                    }
                )
            }.toTypedArray()
        }
        HorizontalTabPager(items)
    }
}

@Composable
fun ZoomableAsyncImageFullSample(model: Any?) {
    val context = LocalContext.current
    ZoomableAsyncImage(
        model = ImageRequest.Builder(context).apply {
            data(model)
            crossfade(true)
        }.build(),
        contentDescription = "",
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        state = rememberZoomableImageState(
            rememberZoomableState(
                zoomSpec = ZoomSpec(maxZoomFactor = 8f)
            )
        )
    )
}