/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.zoomimage.sample.ui.components

import android.os.Bundle
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.panpf.assemblyadapter.recycler.AssemblyRecyclerAdapter
import com.github.panpf.tools4a.dimen.ktx.dp2px
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.sample.databinding.FragmentRecyclerBinding
import com.github.panpf.zoomimage.sample.ui.base.BaseBindingDialogFragment
import com.github.panpf.zoomimage.sample.ui.common.list.InfoItemItemFactory
import com.github.panpf.zoomimage.sample.ui.model.InfoItem
import com.github.panpf.zoomimage.sample.util.format
import com.github.panpf.zoomimage.sample.util.formatFileSize
import com.github.panpf.zoomimage.util.round
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.zoom.toShortString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt

class InfoItemsDialogFragment : BaseBindingDialogFragment<FragmentRecyclerBinding>() {

    private val args by navArgs<InfoItemsDialogFragmentArgs>()
    private val infoItems by lazy { Json.decodeFromString<List<InfoItem>>(args.infoItems) }

    override fun onViewCreated(
        binding: FragmentRecyclerBinding,
        savedInstanceState: Bundle?
    ) {
        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = AssemblyRecyclerAdapter(listOf(InfoItemItemFactory()), infoItems)
            setPadding(0, 20.dp2px, 0, 10.dp2px)
            clipToPadding = false
        }
    }

    companion object {

        fun buildArgs(infoItems: List<InfoItem>): InfoItemsDialogFragmentArgs {
            return InfoItemsDialogFragmentArgs(
                infoItems = Json.encodeToString(infoItems),
            )
        }
    }
}

fun buildZoomImageViewInfos(
    zoomImageView: ZoomImageView,
    sketchImageUri: String
): List<InfoItem> = buildList {
    add(InfoItem(title = null, content = sketchImageUri))

    val zoomable = zoomImageView.zoomable
    val subsampling = zoomImageView.subsampling
    val imageInfo = subsampling.imageInfoState.value
    val transform = zoomable.transformState.value
    val baseTransform = zoomable.baseTransformState.value
    val userTransform = zoomable.userTransformState.value
    val baseInfo = """
        containerSize: ${zoomable.containerSizeState.value.let { "${it.width}x${it.height}" }}
        contentSize: ${zoomable.contentSizeState.value.let { "${it.width}x${it.height}" }}
        contentOriginSize: ${imageInfo?.let { "${it.width}x${it.height}" }}
        rotation: ${transform.rotation.roundToInt()}
    """.trimIndent()
    add(InfoItem(title = "Base: ", content = baseInfo))

    val scaleFormatted = transform.scale.toShortString()
    val baseScaleFormatted = baseTransform.scale.toShortString()
    val userScaleFormatted = userTransform.scale.toShortString()
    val scales = floatArrayOf(
        zoomable.minScaleState.value,
        zoomable.mediumScaleState.value,
        zoomable.maxScaleState.value,
    ).joinToString(prefix = "[", postfix = "]") { it.format(2).toString() }
    val scaleInfo = """
        scale: $scaleFormatted
        baseScale: $baseScaleFormatted
        userScale: $userScaleFormatted
        scales: $scales
    """.trimIndent()
    add(InfoItem(title = "Scale: ", content = scaleInfo))

    val offsetInfo = """
        offset: ${transform.offset.round().toShortString()}
        baseOffset: ${baseTransform.offset.round().toShortString()}
        userOffset: ${userTransform.offset.round().toShortString()}
        userOffsetBounds: ${zoomable.userOffsetBoundsState.value.toShortString()}
        edge: ${zoomable.scrollEdgeState.value.toShortString()}
    """.trimIndent()
    add(InfoItem(title = "Offset: ", content = offsetInfo))

    val displayAndVisibleInfo = """
        contentBaseDisplay: ${zoomable.contentBaseDisplayRectState.value.toShortString()}
        contentBaseVisible: ${zoomable.contentBaseVisibleRectState.value.toShortString()}
        contentDisplay: ${zoomable.contentDisplayRectState.value.toShortString()}
        contentVisible: ${zoomable.contentVisibleRectState.value.toShortString()}
    """.trimIndent()
    add(InfoItem(title = "Display&Visible: ", content = displayAndVisibleInfo))

    val foregroundTiles = subsampling.foregroundTilesState.value
    val loadedTileCount = foregroundTiles.count { it.tileImage != null }
    val loadedTileBytes =
        foregroundTiles.sumOf { it.tileImage?.byteCount ?: 0 }.formatFileSize()
    val backgroundTiles = subsampling.backgroundTilesState.value
    val backgroundTilesLoadedCount = backgroundTiles.count { it.tileImage != null }
    val backgroundTilesLoadedBytes =
        backgroundTiles.sumOf { it.tileImage?.byteCount ?: 0 }.formatFileSize()
    val tileGridSizeMapString = subsampling.tileGridSizeMapState.value.entries
        .joinToString(prefix = "[", postfix = "]", separator = ", ") {
            "${it.key}:${it.value.toShortString()}"
        }
    val tilesInfo = """
        tileGridSizeMap：$tileGridSizeMapString
        sampleSize：${subsampling.sampleSizeState.value}
        imageLoadRect：${subsampling.imageLoadRectState.value.toShortString()}
        foreground：size=${foregroundTiles.size}, load=$loadedTileCount, bytes=$loadedTileBytes
        background：size=${backgroundTiles.size}, load=$backgroundTilesLoadedCount, bytes=$backgroundTilesLoadedBytes
    """.trimIndent()
    add(InfoItem(title = "Tiles: ", content = tilesInfo))
}