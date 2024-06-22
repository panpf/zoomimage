package com.github.panpf.zoomimage.sample.ui.widget

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import com.github.panpf.tools4j.io.ktx.formatCompactFileSize
import com.github.panpf.zoomimage.compose.subsampling.SubsamplingState
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.sample.util.format
import com.github.panpf.zoomimage.sample.ui.util.toShortString
import com.github.panpf.zoomimage.zoom.toShortString
import kotlin.math.roundToInt

@Composable
fun ZoomImageInfo(
    imageUri: String,
    zoomable: ZoomableState,
    subsampling: SubsamplingState,
) {
    val baseInfo = remember(zoomable.transform) {
        val imageInfo = subsampling.imageInfo
        """
            containerSize: ${zoomable.containerSize.let { "${it.width}x${it.height}" }}
            contentSize: ${zoomable.contentSize.let { "${it.width}x${it.height}" }}
            contentOriginSize: ${imageInfo?.let { "${it.width}x${it.height}" }}
            rotation: ${zoomable.transform.rotation.roundToInt()}
        """.trimIndent()
    }
    val scaleInfo = remember(zoomable.transform) {
        val scaleFormatted = zoomable.transform.scale.toShortString()
        val baseScaleFormatted = zoomable.baseTransform.scale.toShortString()
        val userScaleFormatted = zoomable.userTransform.scale.toShortString()
        val scales = floatArrayOf(
            zoomable.minScale,
            zoomable.mediumScale,
            zoomable.maxScale
        ).joinToString(prefix = "[", postfix = "]") { it.format(2).toString() }
        """
            scale: $scaleFormatted
            baseScale: $baseScaleFormatted
            userScale: $userScaleFormatted
            scales: $scales
        """.trimIndent()
    }
    val offsetInfo = remember(zoomable.transform) {
        """
            offset: ${zoomable.transform.offset.round().toShortString()}
            baseOffset: ${zoomable.baseTransform.offset.round().toShortString()}
            userOffset: ${zoomable.userTransform.offset.round().toShortString()}
            userOffsetBounds: ${zoomable.userOffsetBounds.toShortString()}
            edge: ${zoomable.scrollEdge.toShortString()}
        """.trimIndent()
    }
    val displayAndVisibleInfo = remember(zoomable.transform) {
        """
            contentBaseDisplay: ${zoomable.contentBaseDisplayRect.toShortString()}
            contentBaseVisible: ${zoomable.contentBaseVisibleRect.toShortString()}
            contentDisplay: ${zoomable.contentDisplayRect.toShortString()}
            contentVisible: ${zoomable.contentVisibleRect.toShortString()}
        """.trimIndent()
    }
    val tileInfo = remember(zoomable.transform) {
        val foregroundTiles = subsampling.foregroundTiles
        val loadedTileCount = foregroundTiles.count { it.tileBitmap != null }
        val loadedTileBytes =
            foregroundTiles.sumOf { it.tileBitmap?.byteCount ?: 0 }.toLong().formatCompactFileSize()
        val backgroundTiles = subsampling.backgroundTiles
        val backgroundTilesLoadedCount = backgroundTiles.count { it.tileBitmap != null }
        val backgroundTilesLoadedBytes =
            backgroundTiles.sumOf { it.tileBitmap?.byteCount ?: 0 }.toLong().formatCompactFileSize()
        val tileGridSizeMapString = subsampling.tileGridSizeMap.entries
            .joinToString(prefix = "[", postfix = "]", separator = ", ") {
                "${it.key}:${it.value.toShortString()}"
            }
        """
            tileGridSizeMap：$tileGridSizeMapString
            sampleSize：${subsampling.sampleSize}
            imageLoadRect：${subsampling.imageLoadRect.toShortString()}
            foreground：size=${foregroundTiles.size}, load=$loadedTileCount, bytes=$loadedTileBytes
            background：size=${backgroundTiles.size}, load=$backgroundTilesLoadedCount, bytes=$backgroundTilesLoadedBytes
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
        InfoItem("Display&Visible：", displayAndVisibleInfo)

        Spacer(modifier = Modifier.size(8.dp))
        InfoItem("Tiles：", tileInfo)
    }
}

@Composable
fun ColumnScope.InfoItem(title: String?, content: String, contentMaxLines: Int? = null) {
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
        maxLines = contentMaxLines ?: Int.MAX_VALUE,
        overflow = if (contentMaxLines != null) TextOverflow.Ellipsis else TextOverflow.Clip,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
}