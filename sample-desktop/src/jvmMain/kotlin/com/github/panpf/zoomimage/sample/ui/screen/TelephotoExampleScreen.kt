package com.github.panpf.zoomimage.sample.ui.screen

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.panpf.zoomimage.sample.ui.util.toShortString
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable

@Composable
@Preview
fun TelephotoExampleScreen() {
    val zoomableState = rememberZoomableState()
    Image(
        modifier = Modifier.fillMaxSize().zoomable(zoomableState),
        painter = painterResource("sample_huge_china.jpg"),
        contentDescription = "China",
    )
    Column(Modifier.padding(20.dp)) {
        Text(
            text = "scale: ${zoomableState.contentTransformation.scale.toShortString()}",
            style = LocalTextStyle.current.copy(
                shadow = Shadow(offset = Offset(0f, 0f), blurRadius = 10f),
            ),
        )
        Text(
            text = "offset: ${zoomableState.contentTransformation.offset.toShortString()}",
            style = LocalTextStyle.current.copy(
                shadow = Shadow(offset = Offset(0f, 0f), blurRadius = 10f),
            ),
        )
        Text(
            text = "centroid: ${zoomableState.contentTransformation.centroid?.toShortString()}",
            style = LocalTextStyle.current.copy(
                shadow = Shadow(offset = Offset(0f, 0f), blurRadius = 10f),
            ),
        )
    }
}