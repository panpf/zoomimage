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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.sketch.displayImage
import com.github.panpf.sketch.resize.Precision
import com.github.panpf.sketch.stateimage.ThumbnailMemoryCacheStateImage
import com.github.panpf.sketch.viewability.showSectorProgressIndicator
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.sample.databinding.SketchZoomImageViewFragmentBinding
import com.github.panpf.zoomimage.sample.databinding.ZoomImageViewCommonFragmentBinding
import com.github.panpf.zoomimage.sample.settingsService
import com.github.panpf.zoomimage.sample.ui.util.collectWithLifecycle
import com.github.panpf.zoomimage.sample.ui.widget.view.ZoomImageMinimapView

class SketchZoomImageViewFragment :
    BaseZoomImageViewFragment<SketchZoomImageViewFragmentBinding>() {

    private val args by navArgs<SketchZoomImageViewFragmentArgs>()

    override val sketchImageUri: String
        get() = args.imageUri

    override fun getCommonBinding(binding: SketchZoomImageViewFragmentBinding): ZoomImageViewCommonFragmentBinding {
        return binding.common
    }

    override fun getZoomImageView(binding: SketchZoomImageViewFragmentBinding): ZoomImageView {
        return binding.sketchZoomImageViewImage
    }

    override fun onViewCreated(
        binding: SketchZoomImageViewFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(binding, savedInstanceState)

        settingsService.ignoreExifOrientation.sharedFlow.collectWithLifecycle(viewLifecycleOwner) {
            loadData(binding, binding.common, sketchImageUri)
        }

        binding.sketchZoomImageViewImage.showSectorProgressIndicator()
    }

    override fun loadImage(
        binding: SketchZoomImageViewFragmentBinding,
        onCallStart: () -> Unit,
        onCallSuccess: () -> Unit,
        onCallError: () -> Unit
    ) {
        binding.sketchZoomImageViewImage.displayImage(args.imageUri) {
            placeholder(ThumbnailMemoryCacheStateImage())
            crossfade(fadeStart = false)
            ignoreExifOrientation(settingsService.ignoreExifOrientation.value)
            // TODO use requestState
            listener(
                onStart = { onCallStart() },
                onSuccess = { _, _ -> onCallSuccess() },
                onError = { _, _ -> onCallError() },
            )
        }
    }

    override fun loadMinimap(zoomImageMinimapView: ZoomImageMinimapView, sketchImageUri: String) {
        zoomImageMinimapView.displayImage(sketchImageUri) {
            crossfade()
            resizeSize(600, 600)
            resizePrecision(Precision.LESS_PIXELS)
            ignoreExifOrientation(settingsService.ignoreExifOrientation.value)
        }
    }

    class ItemFactory : FragmentItemFactory<String>(String::class) {

        override fun createFragment(
            bindingAdapterPosition: Int,
            absoluteAdapterPosition: Int,
            data: String
        ): Fragment = SketchZoomImageViewFragment().apply {
            arguments = SketchZoomImageViewFragmentArgs(data).toBundle()
        }
    }
}