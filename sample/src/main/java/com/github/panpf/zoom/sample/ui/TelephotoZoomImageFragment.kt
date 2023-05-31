package com.github.panpf.zoom.sample.ui

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest
import com.github.panpf.zoom.sample.R
import com.github.panpf.zoom.sample.ui.base.compose.HorizontalTabPager
import com.github.panpf.zoom.sample.ui.base.compose.Material3ComposeAppBarFragment
import com.github.panpf.zoom.sample.ui.base.compose.PageItem
import com.github.panpf.zoom.sample.ui.util.newCoilResourceUri
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState

class TelephotoZoomImageFragment : Material3ComposeAppBarFragment() {

    override fun getTitle(): String {
        return "Telephoto ZoomImage"
    }

    @Composable
    override fun DrawContent() {
        val context = LocalContext.current
        HorizontalTabPager(
            PageItem(
                data = "Telephoto",
                titleFactory = { it },
                contentFactory = { data, _ ->
                    ZoomableAsyncImageFullSample(context.newCoilResourceUri(R.drawable.dog_hor))
                }
            )
        )
    }
}

@Composable
private fun ZoomableAsyncImageFullSample(uri: Uri) {
    val context = LocalContext.current
    ZoomableAsyncImage(
        model = ImageRequest.Builder(context)
            .data(uri)
            .placeholder(R.drawable.im_placeholder)
            .build(), contentDescription = "",
        modifier = Modifier.fillMaxSize(),
        state = rememberZoomableImageState(
            rememberZoomableState(
                zoomSpec = ZoomSpec(maxZoomFactor = 8f)
            )
        )
    )
}