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

package com.github.panpf.zoomimage.sample.ui.gallery

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.assemblyadapter.pager2.AssemblyFragmentStateAdapter
import com.github.panpf.sketch.cache.CachePolicy
import com.github.panpf.sketch.loadImage
import com.github.panpf.sketch.request.LoadState
import com.github.panpf.sketch.request.disallowAnimatedImage
import com.github.panpf.sketch.resize.Precision.LESS_PIXELS
import com.github.panpf.sketch.transform.BlurTransformation
import com.github.panpf.tools4a.display.ktx.getScreenSize
import com.github.panpf.tools4k.lang.asOrThrow
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.appSettings
import com.github.panpf.zoomimage.sample.databinding.FragmentPhotoPagerBinding
import com.github.panpf.zoomimage.sample.getViewImageLoaderIcon
import com.github.panpf.zoomimage.sample.image.PaletteDecodeInterceptor
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.image.simplePalette
import com.github.panpf.zoomimage.sample.ui.SwitchImageLoaderDialogFragment
import com.github.panpf.zoomimage.sample.ui.ZoomImageSettingsDialogFragment
import com.github.panpf.zoomimage.sample.ui.base.BaseBindingFragment
import com.github.panpf.zoomimage.sample.ui.examples.BasicZoomImageViewFragment
import com.github.panpf.zoomimage.sample.ui.examples.CoilZoomImageViewFragment
import com.github.panpf.zoomimage.sample.ui.examples.GlideZoomImageViewFragment
import com.github.panpf.zoomimage.sample.ui.examples.PicassoZoomImageViewFragment
import com.github.panpf.zoomimage.sample.ui.examples.SketchZoomImageViewFragment
import com.github.panpf.zoomimage.sample.ui.model.Photo
import com.github.panpf.zoomimage.sample.util.collectWithLifecycle
import com.github.panpf.zoomimage.sample.util.repeatCollectWithLifecycle
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class PhotoPagerFragment : BaseBindingFragment<FragmentPhotoPagerBinding>() {

    private val args by navArgs<PhotoPagerFragmentArgs>()
    private val photoList by lazy {
        Json.decodeFromString<List<Photo>>(args.photos)
    }
    private val photoPaletteViewModel by viewModels<PhotoPaletteViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lightStatusAndNavigationBar = false
    }

    override fun getStatusBarInsetsView(binding: FragmentPhotoPagerBinding): View {
        return binding.topBarInsetsLayout
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(
        binding: FragmentPhotoPagerBinding,
        savedInstanceState: Bundle?
    ) {
        binding.backImage.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.bgImage.requestState.loadState.repeatCollectWithLifecycle(
            viewLifecycleOwner,
            State.CREATED
        ) {
            if (it is LoadState.Success) {
                photoPaletteViewModel.setPhotoPalette(
                    PhotoPalette(
                        palette = it.result.simplePalette,
                        primaryColor = ResourcesCompat.getColor(
                            resources, R.color.md_theme_primary, null
                        ),
                        tertiaryColor = ResourcesCompat.getColor(
                            resources, R.color.md_theme_tertiary, null
                        ),
                    )
                )
            }
        }

        binding.orientationImage.apply {
            appSettings.horizontalPagerLayout.collectWithLifecycle(viewLifecycleOwner) {
                val meuIcon = if (it) R.drawable.ic_swap_ver else R.drawable.ic_swap_hor
                setImageResource(meuIcon)
            }
            setOnClickListener {
                appSettings.horizontalPagerLayout.value =
                    !appSettings.horizontalPagerLayout.value
            }
        }

        binding.imageLoaderLayout.setOnClickListener {
            SwitchImageLoaderDialogFragment().show(childFragmentManager, null)
        }

        binding.imageLoaderImage.apply {
            viewLifecycleOwner.lifecycleScope.launch {
                appSettings.viewImageLoader.collect { viewImageLoaderName ->
                    setImageDrawable(getViewImageLoaderIcon(requireContext(), viewImageLoaderName))
                }
            }
        }

        binding.settingsImage.setOnClickListener {
            ZoomImageSettingsDialogFragment().show(childFragmentManager, null)
        }

        binding.pager.apply {
            offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
            appSettings.horizontalPagerLayout.collectWithLifecycle(viewLifecycleOwner) {
                orientation =
                    if (it) ViewPager2.ORIENTATION_HORIZONTAL else ViewPager2.ORIENTATION_VERTICAL
            }

            viewLifecycleOwner.lifecycleScope.launch {
                appSettings.viewImageLoader.collect {
                    adapter = AssemblyFragmentStateAdapter(
                        fragment = this@PhotoPagerFragment,
                        itemFactoryList = listOf(newPhotoDetailItemFactory(requireContext())),
                        initDataList = photoList
                    )
                }
            }

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    val imageUri = photoList[position].listThumbnailUrl
                    loadBgImage(binding, imageUri)
                }
            })

            post {
                setCurrentItem(args.initialPosition - args.startPosition, false)
            }
        }

        binding.pageNumberText.apply {
            val updateCurrentPageNumber: () -> Unit = {
                val pageNumber = args.startPosition + binding.pager.currentItem + 1
                text = resources.getString(R.string.pager_number, pageNumber, args.totalCount)
            }
            binding.pager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateCurrentPageNumber()
                }
            })
            updateCurrentPageNumber()
        }

        photoPaletteViewModel.photoPaletteState.repeatCollectWithLifecycle(
            owner = viewLifecycleOwner,
            state = State.CREATED
        ) { photoPalette ->
            listOf(
                binding.backImage,
                binding.orientationImage,
                binding.imageLoaderLayout,
                binding.settingsImage,
                binding.pageNumberText
            ).forEach {
                it.background.asOrThrow<GradientDrawable>().setColor(photoPalette.containerColorInt)
            }
        }
    }

    private fun loadBgImage(binding: FragmentPhotoPagerBinding, imageUri: String) {
        when (val imageLoaderName = appSettings.viewImageLoader.value) {
            "Sketch" -> loadBgImageBySketch(binding, imageUri)
            // Because it is not easy to implement blurring and calculating Palette in other image loaders, I used Sketch instead, but cannot use memory cache.
            "Coil" -> loadBgImageBySketch(binding, imageUri, CachePolicy.DISABLED)
            "Glide" -> loadBgImageBySketch(binding, imageUri, CachePolicy.DISABLED)
            "Picasso" -> loadBgImageBySketch(binding, imageUri, CachePolicy.DISABLED)
            "Basic" -> loadBgImageBySketch(binding, imageUri)
            else -> throw IllegalArgumentException("Unknown imageLoaderName: $imageLoaderName")
        }
    }

    private fun loadBgImageBySketch(
        binding: FragmentPhotoPagerBinding,
        imageUri: String,
        memoryCachePolicy: CachePolicy = CachePolicy.ENABLED
    ) {
        binding.bgImage.loadImage(imageUri) {
            val screenSize = requireContext().getScreenSize()
            resize(
                width = screenSize.x / 4,
                height = screenSize.y / 4,
                precision = LESS_PIXELS
            )
            addTransformations(
                BlurTransformation(
                    radius = 20,
                    maskColor = ColorUtils.setAlphaComponent(Color.BLACK, 100)
                )
            )
            disallowAnimatedImage()
            memoryCachePolicy(memoryCachePolicy)
            crossfade(alwaysUse = true, durationMillis = 400)
            resizeOnDraw()
            components {
                addDecodeInterceptor(PaletteDecodeInterceptor())
            }
        }
    }
}

fun newPhotoDetailItemFactory(context: Context): FragmentItemFactory<Photo> {
    return when (val imageLoaderName = context.appSettings.viewImageLoader.value) {
        "Sketch" -> SketchZoomImageViewFragment.ItemFactory()
        "Coil" -> CoilZoomImageViewFragment.ItemFactory()
        "Glide" -> GlideZoomImageViewFragment.ItemFactory()
        "Picasso" -> PicassoZoomImageViewFragment.ItemFactory()
        "Basic" -> BasicZoomImageViewFragment.ItemFactory()
        else -> throw IllegalArgumentException("Unknown imageLoaderName: $imageLoaderName")
    }
}