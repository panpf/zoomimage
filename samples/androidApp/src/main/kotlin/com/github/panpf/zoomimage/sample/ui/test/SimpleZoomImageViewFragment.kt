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

package com.github.panpf.zoomimage.sample.ui.test

import android.os.Bundle
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.sketch.loadImage
import com.github.panpf.zoomimage.sample.databinding.FragmentZoomViewSimpleBinding
import com.github.panpf.zoomimage.sample.ui.base.BaseBindingFragment
import com.github.panpf.zoomimage.view.zoom.ScrollBarSpec

class SimpleZoomImageViewFragment : BaseBindingFragment<FragmentZoomViewSimpleBinding>() {

    private val args by navArgs<SimpleZoomImageViewFragmentArgs>()

    override fun onViewCreated(
        binding: FragmentZoomViewSimpleBinding,
        savedInstanceState: Bundle?
    ) {
        binding.zoomImageView.apply {
            scrollBar = ScrollBarSpec.Medium.copy(
                windowInsetsTypeMask = WindowInsetsCompat.Type.navigationBars()
            )
            loadImage(args.imageUri)
        }
    }

    class ItemFactory : FragmentItemFactory<String>(String::class) {

        override fun createFragment(
            bindingAdapterPosition: Int,
            absoluteAdapterPosition: Int,
            data: String
        ): Fragment = SimpleZoomImageViewFragment().apply {
            arguments = SimpleZoomImageViewFragmentArgs(data).toBundle()
        }
    }
}