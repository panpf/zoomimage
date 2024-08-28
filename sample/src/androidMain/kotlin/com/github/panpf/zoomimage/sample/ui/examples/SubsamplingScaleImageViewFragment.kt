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

package com.github.panpf.zoomimage.sample.ui.examples

import android.annotation.SuppressLint
import android.graphics.PointF
import android.graphics.RectF
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView.OnStateChangedListener
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.sketch.fetch.isAssetUri
import com.github.panpf.sketch.util.toUri
import com.github.panpf.zoomimage.sample.databinding.FragmentSubsamplingViewBinding
import com.github.panpf.zoomimage.sample.ui.base.BaseBindingFragment
import com.github.panpf.zoomimage.sample.util.format
import com.github.panpf.zoomimage.sample.util.toShortString
import com.github.panpf.zoomimage.sample.util.toVeryShortString

class SubsamplingScaleImageViewFragment : BaseBindingFragment<FragmentSubsamplingViewBinding>() {

    private val args by navArgs<SubsamplingScaleImageViewFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screenMode = false
    }

    override fun onViewCreated(
        binding: FragmentSubsamplingViewBinding,
        savedInstanceState: Bundle?
    ) {
        binding.subsamplingView.apply {
            setOnStateChangedListener(object : OnStateChangedListener {
                override fun onScaleChanged(newScale: Float, origin: Int) {
                    updateInfo(binding)
                }

                override fun onCenterChanged(newCenter: PointF?, origin: Int) {
                    updateInfo(binding)
                }
            })
        }

        updateInfo(binding)
        setImage(binding)
    }

    private fun setImage(binding: FragmentSubsamplingViewBinding) {
        val imageSource = newImageSource(args.imageUri)
        if (imageSource != null) {
            binding.subsamplingView.setImage(imageSource)
        }
    }

    private fun newImageSource(sketchImageUri: String): ImageSource? {
        val uri = sketchImageUri.toUri()
        if (isAssetUri(uri)) {
            val fileName = uri.pathSegments.drop(1).joinToString("/")
            return ImageSource.asset(fileName)
        }
        return null
    }

    @SuppressLint("SetTextI18n")
    private fun updateInfo(binding: FragmentSubsamplingViewBinding) {
        binding.infoHeaderText.text = """
                scale: 
                space: 
                center: 
            """.trimIndent()
        binding.infoText.text = binding.subsamplingView.run {
            """
                ${scale.format(2)} in (${minScale.format(2)},${maxScale.format(2)})
                ${RectF().apply { getPanRemaining(this) }.toVeryShortString()}
                ${center?.toShortString()}
            """.trimIndent()
        }
    }

    class ItemFactory : FragmentItemFactory<String>(String::class) {

        override fun createFragment(
            bindingAdapterPosition: Int,
            absoluteAdapterPosition: Int,
            data: String
        ): Fragment = SubsamplingScaleImageViewFragment().apply {
            arguments = SubsamplingScaleImageViewFragmentArgs(data).toBundle()
        }
    }
}