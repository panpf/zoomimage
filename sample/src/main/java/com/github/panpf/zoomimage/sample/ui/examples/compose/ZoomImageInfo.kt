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
import com.github.panpf.tools4j.io.ktx.formatCompactFileSize
import com.github.panpf.zoomimage.compose.subsampling.SubsamplingState
import com.github.panpf.zoomimage.compose.subsampling.rememberSubsamplingState
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
    subsamplingState: SubsamplingState,
) {
    val baseInfo = remember(zoomableState.transform) {
        """
            containerSize: ${zoomableState.containerSize.let { "${it.width}x${it.height}" }}
            contentSize: ${zoomableState.contentSize.let { "${it.width}x${it.height}" }}
            contentOriginSize: ${zoomableState.contentOriginSize.let { "${it.width}x${it.height}" }}
            rotation: ${zoomableState.transform.rotation}
        """.trimIndent()
    }
    val scaleInfo = remember(zoomableState.transform) {
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
    val offsetInfo = remember(zoomableState.transform) {
        """
            offset: ${zoomableState.transform.offset.toShortString()}
            baseOffset: ${zoomableState.baseTransform.offset.toShortString()}
            userOffset: ${zoomableState.userTransform.offset.toShortString()}
            userOffsetBounds: ${zoomableState.userOffsetBounds.toShortString()}
            edge: ${zoomableState.scrollEdge.toShortString()}
        """.trimIndent()
    }
    val rectInfo = remember(zoomableState.transform) {
        """
            containerVisible: ${zoomableState.containerVisibleRect.toShortString()}
            contentBaseDisplay: ${zoomableState.contentBaseDisplayRect.toShortString()}
            contentBaseVisible: ${zoomableState.contentBaseVisibleRect.toShortString()}
            contentDisplay: ${zoomableState.contentDisplayRect.toShortString()}
            contentVisible: ${zoomableState.contentVisibleRect.toShortString()}
        """.trimIndent()
    }
    val tileInfo = remember(zoomableState.transform) {
        val tileList = subsamplingState.tileList
        val loadedTileCount = tileList.count { it.bitmap != null }
        val loadedTileBytes =
            tileList.sumOf { it.bitmap?.byteCount ?: 0 }.toLong().formatCompactFileSize()
        """
            tiles=${tileList.size}
            loadedTiles=$loadedTileCount, $loadedTileBytes
        """.trimIndent()
    }

    Column(Modifier.fillMaxWidth()) {
        InfoItem(null, imageUri)

        Spacer(modifier = Modifier.size(8.dp))
        InfoItem("Base", baseInfo)

        Spacer(modifier = Modifier.size(8.dp))
        InfoItem("Scale：", scaleInfo)

        Spacer(modifier = Modifier.size(8.dp))
        InfoItem("Offset：", offsetInfo)

        Spacer(modifier = Modifier.size(8.dp))
        InfoItem("Display&Visible：", rectInfo)

        Spacer(modifier = Modifier.size(8.dp))
        InfoItem("Tile：", tileInfo)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun ZoomImageInfoPreview() {
    val logger = rememberZoomImageLogger()
    ZoomImageInfo(
        imageUri = "https://www.sample.com/sample.jpg",
        zoomableState = rememberZoomableState(logger),
        subsamplingState = rememberSubsamplingState(logger)
    )
}

@Composable
fun ColumnScope.InfoItem(title: String?, content: String) {
    if (title != null) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
    Text(
        text = content,
        modifier = Modifier.fillMaxWidth(),
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
}