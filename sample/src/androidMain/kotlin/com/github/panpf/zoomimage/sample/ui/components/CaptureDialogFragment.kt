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

package com.github.panpf.zoomimage.sample.ui.components

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import com.github.panpf.zoomimage.sample.databinding.FragmentCaptureBinding
import com.github.panpf.zoomimage.sample.ui.base.BaseBindingDialogFragment
import com.github.panpf.zoomimage.sample.ui.examples.CaptureViewModel

class CaptureDialogFragment : BaseBindingDialogFragment<FragmentCaptureBinding>() {

    private val captureViewModel by activityViewModels<CaptureViewModel>()

    override fun onViewCreated(
        binding: FragmentCaptureBinding,
        savedInstanceState: Bundle?
    ) {
        val bitmap = captureViewModel.capturedBitmap
        binding.captureImageView.setImageBitmap(bitmap)

        binding.captureImageView.setOnClickListener {
            dismiss()
        }
    }
}