package com.github.panpf.zoomimage.sample.ui.compose.telephoto

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest.Builder
import com.github.panpf.zoomimage.sample.R.drawable
import com.github.panpf.zoomimage.sample.R.raw
import com.github.panpf.zoomimage.sample.ui.compose.base.AppBarFragment
import com.github.panpf.zoomimage.sample.ui.compose.base.HorizontalTabPager
import com.github.panpf.zoomimage.sample.ui.compose.base.PageItem
import com.github.panpf.zoomimage.sample.ui.view.util.newCoilResourceUri
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState

class TelephotoZoomImageFragment : AppBarFragment() {

    override fun getTitle(): String {
        return "ZoomableAsyncImage（Telephoto）"
    }

    @Composable
    override fun DrawContent() {
        val context = LocalContext.current
        HorizontalTabPager(
            PageItem(
                data = "Telephoto",
                titleFactory = { it },
                contentFactory = { _, _ ->
                    ZoomableAsyncImageFullSample(context.newCoilResourceUri(raw.sample_dog_hor))
                }
            )
        )
    }
}

@Composable
private fun ZoomableAsyncImageFullSample(uri: Uri) {
    val context = LocalContext.current
    ZoomableAsyncImage(
        model = Builder(context)
            .data(uri)
            .placeholder(drawable.im_placeholder)
            .build(), contentDescription = "",
        modifier = Modifier.fillMaxSize(),
        state = rememberZoomableImageState(
            rememberZoomableState(
                zoomSpec = ZoomSpec(maxZoomFactor = 8f)
            )
        )
    )
}