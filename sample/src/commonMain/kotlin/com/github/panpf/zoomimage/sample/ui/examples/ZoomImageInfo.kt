package com.github.panpf.zoomimage.sample.ui.examples

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.round
import com.github.panpf.zoomimage.compose.ZoomState
import com.github.panpf.zoomimage.sample.ui.components.InfoItems
import com.github.panpf.zoomimage.sample.ui.model.InfoItem
import com.github.panpf.zoomimage.sample.ui.model.Photo
import com.github.panpf.zoomimage.sample.ui.util.toShortString
import com.github.panpf.zoomimage.sample.util.format
import com.github.panpf.zoomimage.sample.util.formatFileSize
import com.github.panpf.zoomimage.zoom.toShortString
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.roundToInt

@Composable
fun ZoomImageInfo(photo: Photo, zoomState: ZoomState) {
    val zoomable = zoomState.zoomable
    val subsampling = zoomState.subsampling
    val items by remember {
        derivedStateOf {
            buildList {
                add(InfoItem(null, photo.originalUrl))

                val imageInfo = subsampling.imageInfo
                val baseInfo = """
                        containerSize: ${zoomable.containerSize.let { "${it.width}x${it.height}" }}
                        contentSize: ${zoomable.contentSize.let { "${it.width}x${it.height}" }}
                        contentOriginSize: ${imageInfo?.let { "${it.width}x${it.height}" }}
                        rotation: ${zoomable.transform.rotation.roundToInt()}
                    """.trimIndent()
                add(InfoItem("Base", baseInfo))

                val scaleFormatted = zoomable.transform.scale.toShortString()
                val baseScaleFormatted = zoomable.baseTransform.scale.toShortString()
                val userScaleFormatted = zoomable.userTransform.scale.toShortString()
                val scales = floatArrayOf(
                    zoomable.minScale,
                    zoomable.mediumScale,
                    zoomable.maxScale
                ).joinToString(prefix = "[", postfix = "]") { it.format(2).toString() }
                val scaleInfo = """
                        scale: $scaleFormatted
                        baseScale: $baseScaleFormatted
                        userScale: $userScaleFormatted
                        scales: $scales
                    """.trimIndent()
                add(InfoItem("Scale：", scaleInfo))

                val offsetInfo = """
                        offset: ${zoomable.transform.offset.round().toShortString()}
                        baseOffset: ${zoomable.baseTransform.offset.round().toShortString()}
                        userOffset: ${zoomable.userTransform.offset.round().toShortString()}
                        userOffsetBounds: ${zoomable.userOffsetBounds.toShortString()}
                        edge: ${zoomable.scrollEdge.toShortString()}
                    """.trimIndent()
                add(InfoItem("Offset：", offsetInfo))

                val displayAndVisibleInfo = """
                        contentBaseDisplay: ${zoomable.contentBaseDisplayRect.toShortString()}
                        contentBaseVisible: ${zoomable.contentBaseVisibleRect.toShortString()}
                        contentDisplay: ${zoomable.contentDisplayRect.toShortString()}
                        contentVisible: ${zoomable.contentVisibleRect.toShortString()}
                    """.trimIndent()
                add(InfoItem("Display&Visible：", displayAndVisibleInfo))

                val foregroundTiles = subsampling.foregroundTiles
                val loadedTileCount = foregroundTiles.count { it.tileImage != null }
                val loadedTileBytes =
                    foregroundTiles.sumOf { it.tileImage?.byteCount ?: 0 }.formatFileSize()
                val backgroundTiles = subsampling.backgroundTiles
                val backgroundTilesLoadedCount = backgroundTiles.count { it.tileImage != null }
                val backgroundTilesLoadedBytes =
                    backgroundTiles.sumOf { it.tileImage?.byteCount ?: 0 }.formatFileSize()
                val tileGridSizeMapString = subsampling.tileGridSizeMap.entries
                    .joinToString(prefix = "[", postfix = "]", separator = ", ") {
                        "${it.key}:${it.value.toShortString()}"
                    }
                val tileInfo = """
                        tileGridSizeMap：$tileGridSizeMapString
                        sampleSize：${subsampling.sampleSize}
                        imageLoadRect：${subsampling.imageLoadRect.toShortString()}
                        foreground：size=${foregroundTiles.size}, load=$loadedTileCount, bytes=$loadedTileBytes
                        background：size=${backgroundTiles.size}, load=$backgroundTilesLoadedCount, bytes=$backgroundTilesLoadedBytes
                    """.trimIndent()
                add(InfoItem("Tiles：", tileInfo))
            }.toImmutableList()
        }
    }
    InfoItems(items)
}