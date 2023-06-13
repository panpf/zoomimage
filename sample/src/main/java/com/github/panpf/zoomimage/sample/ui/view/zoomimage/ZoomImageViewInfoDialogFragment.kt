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
package com.github.panpf.zoomimage.sample.ui.view.zoomimage

import android.os.Bundle
import androidx.navigation.fragment.navArgs
import com.github.panpf.zoomimage.sample.databinding.ZoomImageViewInfoDialogBinding
import com.github.panpf.zoomimage.sample.ui.view.base.BindingDialogFragment

class ZoomImageViewInfoDialogFragment : BindingDialogFragment<ZoomImageViewInfoDialogBinding>() {

    private val args by navArgs<ZoomImageViewInfoDialogFragmentArgs>()

    override fun onViewCreated(binding: ZoomImageViewInfoDialogBinding, savedInstanceState: Bundle?) {
        binding.imageInfoUriContent.text = args.imageUri
        binding.imageInfoImageContent.text = args.imageInfo
        binding.imageInfoSizeContent.text = args.sizeInfo
        binding.imageInfoTilesContent.text = args.tilesInfo
    }
}