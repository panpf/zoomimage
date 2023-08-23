/*
 * Copyright (C) 2022 panpf <panpfpanpf@outlook.com>
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
import com.github.panpf.sketch.decode.internal.exifOrientationName
import com.github.panpf.tools4j.io.ktx.formatFileSize
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.sample.databinding.ZoomImageViewInfoDialogBinding
import com.github.panpf.zoomimage.sample.ui.base.view.BindingDialogFragment
import com.github.panpf.zoomimage.sample.util.format
import com.github.panpf.zoomimage.toShortString
import com.github.panpf.zoomimage.util.round
import com.github.panpf.zoomimage.util.toShortString
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
        binding.imageInfoTileText.text = args.tileInfo
    }

    companion object {

        fun buildArgs(
            zoomImageView: ZoomImageView,
            sketchImageUri: String
        ): ZoomImageViewInfoDialogFragmentArgs {
            val zoomAbility = zoomImageView.zoomAbility
            val subsamplingAbility = zoomImageView.subsamplingAbility

            val exifOrientationName = subsamplingAbility.imageInfo
                ?.exifOrientation?.let { exifOrientationName(it) }
            val baseInfo = """
                containerSize: ${zoomAbility.containerSize.let { "${it.width}x${it.height}" }}
                contentSize: ${zoomAbility.contentSize.let { "${it.width}x${it.height}" }}
                contentOriginSize: ${zoomAbility.contentOriginSize.let { "${it.width}x${it.height}" }}
                exifOrientation: $exifOrientationName
                rotation: ${zoomAbility.transform.rotation.roundToInt()}
            """.trimIndent()

            val scaleFormatted = zoomAbility.transform.scale.toShortString()
            val baseScaleFormatted = zoomAbility.baseTransform.scale.toShortString()
            val userScaleFormatted = zoomAbility.userTransform.scale.toShortString()
            val scales = floatArrayOf(
                zoomAbility.minScale,
                zoomAbility.mediumScale,
                zoomAbility.maxScale
            ).joinToString(prefix = "[", postfix = "]") { it.format(2).toString() }

            val scaleInfo = """
                scale: $scaleFormatted
                baseScale: $baseScaleFormatted
                userScale: $userScaleFormatted
                stepScales: $scales
            """.trimIndent()

            val offsetInfo = """
                offset: ${zoomAbility.transform.offset.round().toShortString()}
                baseOffset: ${zoomAbility.baseTransform.offset.round().toShortString()}
                userOffset: ${zoomAbility.userTransform.offset.round().toShortString()}
                userOffsetBounds: ${zoomAbility.userOffsetBounds.toShortString()}
                edge: ${zoomAbility.scrollEdge.toShortString()}
            """.trimIndent()

            val displayAndVisibleInfo = """
                contentBaseDisplay: ${zoomAbility.contentBaseDisplayRect.toShortString()}
                contentBaseVisible: ${zoomAbility.contentBaseVisibleRect.toShortString()}
                contentDisplay: ${zoomAbility.contentDisplayRect.toShortString()}
                contentVisible: ${zoomAbility.contentVisibleRect.toShortString()}
            """.trimIndent()

            val tileList = subsamplingAbility.tileList
            val loadedTileCount = tileList.count { it.bitmap != null }
            val loadedTileBytes =
                tileList.sumOf { it.bitmap?.byteCount ?: 0 }.toLong().formatFileSize()
            val tilesInfo = """
                tiles=${tileList.size}
                loadedTiles=$loadedTileCount, $loadedTileBytes
            """.trimIndent()
            return ZoomImageViewInfoDialogFragmentArgs(
                imageUri = sketchImageUri,
                baseInfo = baseInfo,
                scaleInfo = scaleInfo,
                offsetInfo = offsetInfo,
                displayAndVisibleInfo = displayAndVisibleInfo,
                tileInfo = tilesInfo
            )
        }
    }
}