package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.request.ImageRequest
import com.github.panpf.zoomimage.sample.util.format
import com.github.panpf.zoomimage.sample.util.sketchUri2CoilModel
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState

@Composable
fun TelephotoZoomableAsyncImageSample(sketchImageUri: String) {
    val context = LocalContext.current
    val coilData =
        remember(key1 = sketchImageUri) { sketchUri2CoilModel(context, sketchImageUri) }
    val zoomableState = rememberZoomableState(
        zoomSpec = ZoomSpec(maxZoomFactor = 8f)
    )
    val info = remember(zoomableState.contentTransformation) {
        zoomableState.contentTransformation.run {
            """
                scale: ${scale.scaleX.format(2)}, ${scale.scaleY.format(2)}
                offset: (${offset.x.format(2)}x${offset.y.format(2)})
            """.trimIndent()
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        ZoomableAsyncImage(
            model = ImageRequest.Builder(LocalContext.current).apply {
                data(coilData)
                crossfade(true)
            }.build(),
            contentDescription = "",
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            state = rememberZoomableImageState(zoomableState),
        )

        Text(
            text = info,
            color = Color.White,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            style = LocalTextStyle.current.copy(
                shadow = Shadow(offset = Offset(1f, 1f), blurRadius = 4f),
            ),
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(10.dp)
        )
    }
}