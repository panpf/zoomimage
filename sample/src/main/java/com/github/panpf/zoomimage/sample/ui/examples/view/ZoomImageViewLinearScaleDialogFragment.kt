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
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.navArgs
import com.github.panpf.zoomimage.sample.databinding.ZoomImageViewLinearScaleDialogBinding
import com.github.panpf.zoomimage.sample.ui.base.view.BindingDialogFragment
import com.github.panpf.zoomimage.sample.ui.base.view.parentViewModels
import com.github.panpf.zoomimage.sample.util.format
import kotlinx.coroutines.launch

class ZoomImageViewLinearScaleDialogFragment :
    BindingDialogFragment<ZoomImageViewLinearScaleDialogBinding>() {

    private val args by navArgs<ZoomImageViewLinearScaleDialogFragmentArgs>()
    private val linearScaleViewModel by parentViewModels<LinearScaleViewModel>()

    override fun onViewCreated(
        binding: ZoomImageViewLinearScaleDialogBinding,
        savedInstanceState: Bundle?
    ) {
        binding.zoomImageViewLinearScaleMinScale.text = args.valueFrom.format(1).toString()
        binding.zoomImageViewLinearScaleMaxScale.text = args.valueTo.format(1).toString()
        binding.zoomImageViewLinearScaleSlider.apply {
            valueFrom = args.valueFrom
            valueTo = args.valueTo
            val step = (args.valueTo - args.valueFrom) / 9
            value = args.valueFrom + ((args.value - args.valueFrom) / step).toInt() * step
            stepSize = step
            addOnChangeListener { _, value, _ ->
                viewLifecycleOwner.lifecycle.coroutineScope.launch {
                    linearScaleViewModel.changeFlow.emit(value)
                }
            }
        }
    }
}