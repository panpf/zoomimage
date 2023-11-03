/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
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

package com.github.panpf.zoomimage.sample.ui.examples.view

import android.os.Bundle
import androidx.navigation.fragment.navArgs
import com.github.panpf.tools4j.io.ktx.formatCompactFileSize
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.sample.databinding.ZoomImageViewInfoDialogBinding
import com.github.panpf.zoomimage.sample.ui.base.view.BindingDialogFragment
import com.github.panpf.zoomimage.sample.util.format
import com.github.panpf.zoomimage.util.round
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.zoom.toShortString
import kotlin.math.roundToInt

class ZoomImageViewInfoDialogFragment : BindingDialogFragment<ZoomImageViewInfoDialogBinding>() {

    private val args by navArgs<ZoomImageViewInfoDialogFragmentArgs>()

    override fun onViewCreated(
        binding: ZoomImageViewInfoDialogBinding,
        savedInstanceState: Bundle?
    ) {
        binding.imageInfoUriText.text = args.imageUri
        binding.imageInfoBaseInfoText.text = args.baseInfo
        binding.imageInfoScaleText.text = args.scaleInfo
        binding.imageInfoOffsetText.text = args.offsetInfo
        binding.imageInfoDisplayAndVisibleText.text = args.displayAndVisibleInfo
        binding.imageInfoTilesText.text = args.tilesInfo
    }

    companion object {

        fun buildArgs(
            zoomImageView: ZoomImageView,
            sketchImageUri: String
        ): ZoomImageViewInfoDialogFragmentArgs {
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
                exifOrientation: ${subsampling.exifOrientationState.value?.name()}
                rotation: ${transform.rotation.roundToInt()}
            """.trimIndent()

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

            val offsetInfo = """
                offset: ${transform.offset.round().toShortString()}
                baseOffset: ${baseTransform.offset.round().toShortString()}
                userOffset: ${userTransform.offset.round().toShortString()}
                userOffsetBounds: ${zoomable.userOffsetBoundsState.value.toShortString()}
                edge: ${zoomable.scrollEdgeState.value.toShortString()}
            """.trimIndent()

            val displayAndVisibleInfo = """
                contentBaseDisplay: ${zoomable.contentBaseDisplayRectState.value.toShortString()}
                contentBaseVisible: ${zoomable.contentBaseVisibleRectState.value.toShortString()}
                contentDisplay: ${zoomable.contentDisplayRectState.value.toShortString()}
                contentVisible: ${zoomable.contentVisibleRectState.value.toShortString()}
            """.trimIndent()

            val foregroundTiles = subsampling.foregroundTilesState.value
            val loadedTileCount = foregroundTiles.count { it.bitmap != null }
            val loadedTileBytes =
                foregroundTiles.sumOf { it.bitmap?.byteCount ?: 0 }.toLong().formatCompactFileSize()
            val backgroundTiles = subsampling.backgroundTilesState.value
            val backgroundTilesLoadedCount = backgroundTiles.count { it.bitmap != null }
            val backgroundTilesLoadedBytes =
                backgroundTiles.sumOf { it.bitmap?.byteCount ?: 0 }.toLong().formatCompactFileSize()
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
            return ZoomImageViewInfoDialogFragmentArgs(
                imageUri = sketchImageUri,
                baseInfo = baseInfo,
                scaleInfo = scaleInfo,
                offsetInfo = offsetInfo,
                displayAndVisibleInfo = displayAndVisibleInfo,
                tilesInfo = tilesInfo
            )
        }
    }
}