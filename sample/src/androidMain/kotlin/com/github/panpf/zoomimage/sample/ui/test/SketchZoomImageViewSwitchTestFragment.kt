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
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.panpf.assemblyadapter.recycler.AssemblyRecyclerAdapter
import com.github.panpf.sketch.loadImage
import com.github.panpf.zoomimage.sample.AppEvents
import com.github.panpf.zoomimage.sample.databinding.FragmentZoomViewSwitchBinding
import com.github.panpf.zoomimage.sample.ui.base.BaseToolbarBindingFragment
import com.github.panpf.zoomimage.sample.ui.common.list.ImageThumbnailItemFactory
import com.github.panpf.zoomimage.sample.util.repeatCollectWithLifecycle
import com.github.panpf.zoomimage.util.toShortString
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.math.roundToInt

class SketchZoomImageViewSwitchTestFragment :
    BaseToolbarBindingFragment<FragmentZoomViewSwitchBinding>() {

    private val viewModel by viewModels<ImageSwitchViewModel>()
    private val appEvents: AppEvents by inject()

    override fun getNavigationBarInsetsView(binding: FragmentZoomViewSwitchBinding): View {
        return binding.root
    }

    override fun onViewCreated(
        toolbar: Toolbar,
        binding: FragmentZoomViewSwitchBinding,
        savedInstanceState: Bundle?
    ) {
        toolbar.title = "SketchZoomImageView (Switch)"

        binding.zoomImageView.zoomable.transformState
            .repeatCollectWithLifecycle(viewLifecycleOwner, Lifecycle.State.CREATED) {
                updateInfo(binding)
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

        binding.switchButton.apply {
            val zoomable = binding.zoomImageView.zoomable
            isChecked = zoomable.keepTransformWhenSameAspectRatioContentSizeChangedState.value
            setOnCheckedChangeListener { _, isChecked ->
                zoomable.keepTransformWhenSameAspectRatioContentSizeChangedState.value = isChecked
                if (isChecked) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        appEvents.toastFlow.emit("Keep Transform only when pictures with the same aspect ratio switch")
                    }
                }
            }
        }

        binding.rotateButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val zoomable = binding.zoomImageView.zoomable
                zoomable.rotateBy(90)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentImageUri.collect { imageUri ->
                setImage(binding, imageUri)
            }
        }

        updateInfo(binding)
    }

    private fun setImage(binding: FragmentZoomViewSwitchBinding, imageUri: String) {
        binding.zoomImageView.loadImage(imageUri)
    }

    @SuppressLint("SetTextI18n")
    private fun updateInfo(binding: FragmentZoomViewSwitchBinding) {
        binding.infoHeaderText.text = """
                scale: 
                offset: 
                rotation: 
            """.trimIndent()
        binding.infoContentText.text =
            binding.zoomImageView.zoomable.transformState.value.run {
                """
                ${scale.toShortString()}
                ${offset.toShortString()}
                ${rotation.roundToInt()}
            """.trimIndent()
            }
    }
}