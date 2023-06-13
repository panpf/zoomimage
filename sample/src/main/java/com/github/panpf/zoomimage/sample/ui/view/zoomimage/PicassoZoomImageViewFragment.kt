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

import android.net.Uri
import android.os.Bundle
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.sample.databinding.ZoomImageViewCommonFragmentBinding
import com.github.panpf.zoomimage.sample.databinding.PicassoZoomImageViewFragmentBinding
import com.github.panpf.zoomimage.sample.prefsService
import com.github.panpf.zoomimage.sample.util.collectWithLifecycle
import com.github.panpf.zoomimage.sample.util.lifecycleOwner
import com.squareup.picasso.Callback
import com.squareup.picasso.RequestCreator
import kotlinx.coroutines.flow.merge
import java.io.File

class PicassoZoomImageViewFragment :
    BaseZoomImageViewFragment<PicassoZoomImageViewFragmentBinding>() {

    private val args by navArgs<CoilZoomImageViewFragmentArgs>()

    override val sketchImageUri: String
        get() = args.imageUri

    override val supportDisabledMemoryCache: Boolean
        get() = true

    override val supportIgnoreExifOrientation: Boolean
        get() = false

    override val supportDisallowReuseBitmap: Boolean
        get() = false

    override fun getCommonBinding(binding: PicassoZoomImageViewFragmentBinding): ZoomImageViewCommonFragmentBinding {
        return binding.common
    }

    override fun getZoomImageView(binding: PicassoZoomImageViewFragmentBinding): ZoomImageView {
        return binding.picassoZoomImageViewImage
    }

    override fun onViewCreated(
        binding: PicassoZoomImageViewFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(binding, savedInstanceState)

        binding.picassoZoomImageViewImage.apply {
//            listOf(
//                prefsService.disableMemoryCache.stateFlow,
//                prefsService.disallowReuseBitmap.stateFlow,
//                prefsService.ignoreExifOrientation.stateFlow,
//            ).merge().collect(lifecycleOwner) {
//                subsamplingAbility.disableMemoryCache = prefsService.disableMemoryCache.value
//                subsamplingAbility.disallowReuseBitmap = prefsService.disallowReuseBitmap.value
//                subsamplingAbility.ignoreExifOrientation = prefsService.ignoreExifOrientation.value
//            }
            listOf(
                prefsService.disableMemoryCache.sharedFlow,
//                prefsService.disallowReuseBitmap.sharedFlow,
//                prefsService.ignoreExifOrientation.sharedFlow,
            ).merge().collectWithLifecycle(lifecycleOwner) {
                loadData(binding, binding.common, sketchImageUri)
            }
        }
    }

    override fun loadImage(
        binding: PicassoZoomImageViewFragmentBinding,
        onCallStart: () -> Unit,
        onCallSuccess: () -> Unit,
        onCallError: () -> Unit
    ) {
        onCallStart()
        val callback = object : Callback {
            override fun onSuccess() {
                onCallSuccess()
            }

            override fun onError(e: Exception?) {
                onCallError()
            }
        }
        val config: RequestCreator.() -> Unit = {
            fit()
            centerInside()
            if (prefsService.disableMemoryCache.value) {
                memoryPolicy(
                    com.squareup.picasso.MemoryPolicy.NO_CACHE,
                    com.squareup.picasso.MemoryPolicy.NO_STORE
                )
            }
        }
        val sketchImageUri = args.imageUri
        when {
            sketchImageUri.startsWith("asset://") ->
                binding.picassoZoomImageViewImage.loadImage(
                    path = sketchImageUri.replace("asset://", "file:///android_asset/"),
                    callback = callback,
                    config = config
                )

            sketchImageUri.startsWith("file://") ->
                binding.picassoZoomImageViewImage.loadImage(
                    file = File(sketchImageUri.replace("file://", "")),
                    callback = callback,
                    config = config
                )

            sketchImageUri.startsWith("android.resource://") -> {
                val resId =
                    sketchImageUri.toUri().getQueryParameters("resId").firstOrNull()?.toIntOrNull()
                if (resId != null) {
                    binding.picassoZoomImageViewImage.loadImage(
                        resourceId = resId,
                        callback = callback,
                        config = config
                    )
                } else {
                    binding.picassoZoomImageViewImage.zoomAbility.logger.w("ZoomImageViewFragment") {
                        "Can't use Subsampling, invalid resource uri: '$sketchImageUri'"
                    }
                    binding.picassoZoomImageViewImage.loadImage(
                        path = null,
                        callback = callback,
                        config = config
                    )
                }
            }

            else ->
                binding.picassoZoomImageViewImage.loadImage(
                    uri = Uri.parse(sketchImageUri),
                    callback = callback,
                    config = config
                )
        }
    }

    class ItemFactory : FragmentItemFactory<String>(String::class) {

        override fun createFragment(
            bindingAdapterPosition: Int,
            absoluteAdapterPosition: Int,
            data: String
        ): Fragment = PicassoZoomImageViewFragment().apply {
            arguments = PicassoZoomImageViewFragmentArgs(data).toBundle()
        }
    }
}