package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import com.github.panpf.zoomimage.rememberZoomImageLogger
import com.github.panpf.zoomimage.sample.ui.util.compose.toShortString
import com.github.panpf.zoomimage.sample.util.format
import com.github.panpf.zoomimage.toShortString

@Composable
fun ZoomImageInfo(
    imageUri: String,
    zoomableState: ZoomableState,
) {
    val baseInfo = remember(
        zoomableState.containerSize,
        zoomableState.contentSize,
        zoomableState.contentOriginSize
    ) {
        """
            imageUri: $imageUri
            containerSize: ${zoomableState.containerSize.let { "${it.width}x${it.height}" }}
            contentSize: ${zoomableState.contentSize.let { "${it.width}x${it.height}" }}
            contentOriginSize: ${zoomableState.contentOriginSize.let { "${it.width}x${it.height}" }}
        """.trimIndent()
    }
    val scaleInfo = remember(
        zoomableState.transform,
    ) {
        val transform = zoomableState.transform
        val userTransform = zoomableState.userTransform
        val baseTransform = zoomableState.baseTransform
        val userScaleFormatted = userTransform.scale.toShortString()
        val scaleFormatted = transform.scale.toShortString()
        val baseScaleFormatted = baseTransform.scale.toShortString()
        val scales = floatArrayOf(
            zoomableState.minScale,
            zoomableState.mediumScale,
            zoomableState.maxScale
        ).joinToString(prefix = "[", postfix = "]") { it.format(2).toString() }
        """
            scale: $scaleFormatted
            baseScale: $baseScaleFormatted
            userScale: $userScaleFormatted
            stepScales: $scales
        """.trimIndent()
    }
    val offsetInfo = remember(
        zoomableState.transform,
    ) {
        """
            offset: ${zoomableState.transform.offset.toShortString()}
            baseOffset: ${zoomableState.baseTransform.offset.toShortString()}
            userOffset: ${zoomableState.userTransform.offset.toShortString()}
            userOffsetBounds: ${zoomableState.userOffsetBounds.toShortString()}
            edge: ${zoomableState.scrollEdge.toShortString()}
        """.trimIndent()
    }
    val otherInfo = remember(zoomableState.transform) {
        """
            containerVisible: ${zoomableState.containerVisibleRect.toShortString()}
            contentBaseDisplay: ${zoomableState.contentBaseDisplayRect.toShortString()}
            contentBaseVisible: ${zoomableState.contentBaseVisibleRect.toShortString()}
            contentDisplay: ${zoomableState.contentDisplayRect.toShortString()}
            contentVisible: ${zoomableState.contentVisibleRect.toShortString()}
        """.trimIndent()
    }

    Column(Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.size(12.dp))
        InfoItem("Base", baseInfo)

        Spacer(modifier = Modifier.size(12.dp))
        InfoItem("Scale：", scaleInfo)

        Spacer(modifier = Modifier.size(12.dp))
        InfoItem("Offset：", offsetInfo)

        Spacer(modifier = Modifier.size(12.dp))
        InfoItem("Rect：", otherInfo)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun ZoomImageInfoPreview() {
    ZoomImageInfo(
        imageUri = "https://www.github.com/panpf/zoomimage",
        zoomableState = rememberZoomableState(rememberZoomImageLogger())
    )
}

@Composable
fun ColumnScope.InfoItem(title: String, content: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = content,
        modifier = Modifier.fillMaxWidth(),
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
}