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
package com.github.panpf.zoomimage.sample.ui.subsamplingview

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.davemorrissey.labs.subscaleview.ImageSource
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.zoomimage.sample.databinding.SubsamplingViewFragmentBinding
import com.github.panpf.zoomimage.sample.ui.subsamplingview.SubsamplingViewFragmentArgs
import com.github.panpf.zoomimage.sample.ui.base.BindingFragment

class SubsamplingViewFragment : BindingFragment<SubsamplingViewFragmentBinding>() {

    private val args by navArgs<SubsamplingViewFragmentArgs>()

    override fun onViewCreated(
        binding: SubsamplingViewFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        binding.subsamplingView.setImage(ImageSource.asset(args.imageUri.replace("asset://", "")))
    }

    class ItemFactory : FragmentItemFactory<String>(String::class) {

        override fun createFragment(
            bindingAdapterPosition: Int,
            absoluteAdapterPosition: Int,
            data: String
        ): Fragment = SubsamplingViewFragment().apply {
            arguments = SubsamplingViewFragmentArgs(data).toBundle()
        }
    }
}