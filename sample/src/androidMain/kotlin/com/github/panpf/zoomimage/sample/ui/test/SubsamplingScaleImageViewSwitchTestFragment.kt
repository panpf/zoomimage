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

import android.annotation.SuppressLint
import android.graphics.PointF
import android.graphics.RectF
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView.OnStateChangedListener
import com.github.panpf.assemblyadapter.recycler.AssemblyRecyclerAdapter
import com.github.panpf.sketch.fetch.isAssetUri
import com.github.panpf.sketch.util.toUri
import com.github.panpf.zoomimage.sample.databinding.FragmentSubsamplingViewSwitchBinding
import com.github.panpf.zoomimage.sample.ui.base.BaseToolbarBindingFragment
import com.github.panpf.zoomimage.sample.ui.common.list.ImageThumbnailItemFactory
import com.github.panpf.zoomimage.sample.util.format
import com.github.panpf.zoomimage.sample.util.toShortString
import com.github.panpf.zoomimage.sample.util.toVeryShortString
import kotlinx.coroutines.launch

class SubsamplingScaleImageViewSwitchTestFragment :
    BaseToolbarBindingFragment<FragmentSubsamplingViewSwitchBinding>() {

    private val viewModel by viewModels<ImageSwitchViewModel>()

    override fun getNavigationBarInsetsView(binding: FragmentSubsamplingViewSwitchBinding): View {
        return binding.root
    }

    override fun onViewCreated(
        toolbar: Toolbar,
        binding: FragmentSubsamplingViewSwitchBinding,
        savedInstanceState: Bundle?
    ) {
        toolbar.title = "SubsamplingScaleImageView (Switch)"

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

        binding.images.apply {
            layoutManager = LinearLayoutManager(
                /* context = */ requireContext(),
                /* orientation = */ LinearLayoutManager.HORIZONTAL,
                /* reverseLayout = */ false
            )
            adapter = AssemblyRecyclerAdapter(
                itemFactoryList = listOf(ImageThumbnailItemFactory {
                    viewModel.setImageUri(it)
                }),
                initDataList = viewModel.imageUris
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentImageUri.collect { imageUri ->
                setImage(binding, imageUri)
            }
        }

        updateInfo(binding)
    }

    private fun setImage(binding: FragmentSubsamplingViewSwitchBinding, imageUri: String) {
        val imageSource = newImageSource(imageUri)
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
    private fun updateInfo(binding: FragmentSubsamplingViewSwitchBinding) {
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
}