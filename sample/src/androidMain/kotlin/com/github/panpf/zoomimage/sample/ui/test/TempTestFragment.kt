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
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.sample.databinding.FragmentTempTestBinding
import com.github.panpf.zoomimage.sample.ui.base.BaseToolbarBindingFragment
import com.github.panpf.zoomimage.util.Logger

class TempTestFragment : BaseToolbarBindingFragment<FragmentTempTestBinding>() {

    override fun onViewCreated(
        toolbar: Toolbar,
        binding: FragmentTempTestBinding,
        savedInstanceState: Bundle?
    ) {
        toolbar.title = "Temp"

        binding.glideZoomImageView.logger.level = Logger.Level.Verbose

        Glide.with(binding.glideZoomImageView)
            .load(ResourceImages.hugeChina.uri)
            .thumbnail(
                Glide.with(binding.glideZoomImageView)
                    .load(ResourceImages.hugeChinaThumbnail.uri)
                    .override(120, 120)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
            )
            .priority(Priority.IMMEDIATE)
            .encodeQuality(100)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .format(DecodeFormat.PREFER_ARGB_8888)
            .into(binding.glideZoomImageView)
    }
}