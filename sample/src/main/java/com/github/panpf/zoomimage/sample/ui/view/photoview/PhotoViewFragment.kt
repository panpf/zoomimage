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
package com.github.panpf.zoomimage.sample.ui.view.photoview

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.sketch.displayImage
import com.github.panpf.zoomimage.sample.databinding.PhotoViewFragmentBinding
import com.github.panpf.zoomimage.sample.ui.view.base.BindingFragment

class PhotoViewFragment : BindingFragment<PhotoViewFragmentBinding>() {

    private val args by navArgs<PhotoViewFragmentArgs>()

    override fun onViewCreated(
        binding: PhotoViewFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        binding.photoViewUriText.text = "uri: ${args.imageUri}"

        binding.photoViewErrorRetryButton.setOnClickListener {
            setImage(binding)
        }

        setImage(binding)
    }

    private fun setImage(binding: PhotoViewFragmentBinding) {
        binding.photoView.displayImage(args.imageUri) {
            lifecycle(viewLifecycleOwner.lifecycle)
            crossfade()
            listener(
                onStart = {
                    binding.photoViewProgress.isVisible = true
                    binding.photoViewErrorLayout.isVisible = false
                },
                onSuccess = { _, _ ->
                    binding.photoViewProgress.isVisible = false
                    binding.photoViewErrorLayout.isVisible = false
                },
                onError = { _, _ ->
                    binding.photoViewProgress.isVisible = false
                    binding.photoViewErrorLayout.isVisible = true
                },
            )
        }
    }

    class ItemFactory : FragmentItemFactory<String>(String::class) {

        override fun createFragment(
            bindingAdapterPosition: Int,
            absoluteAdapterPosition: Int,
            data: String
        ): Fragment = PhotoViewFragment().apply {
            arguments = PhotoViewFragmentArgs(data).toBundle()
        }
    }
}