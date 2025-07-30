package com.github.panpf.zoomimage.sample.ui.test

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.assemblyadapter.pager2.AssemblyFragmentStateAdapter
import com.github.panpf.zoomimage.sample.AppSettings
import com.github.panpf.zoomimage.sample.databinding.FragmentTabPagerBinding
import com.github.panpf.zoomimage.sample.ui.base.BaseToolbarBindingFragment
import com.github.panpf.zoomimage.sample.ui.examples.BasicZoomImageViewFragment
import com.github.panpf.zoomimage.sample.ui.examples.CoilZoomImageViewFragment
import com.github.panpf.zoomimage.sample.ui.examples.GlideZoomImageViewFragment
import com.github.panpf.zoomimage.sample.ui.examples.PicassoZoomImageViewFragment
import com.github.panpf.zoomimage.sample.ui.examples.SketchZoomImageViewFragment
import com.github.panpf.zoomimage.sample.ui.model.Photo
import com.google.android.material.tabs.TabLayoutMediator

class ExifOrientationTestFragment : BaseToolbarBindingFragment<FragmentTabPagerBinding>() {

    override fun onViewCreated(
        toolbar: Toolbar, binding: FragmentTabPagerBinding, savedInstanceState: Bundle?
    ) {
        toolbar.title = "Exif Orientation"

        val exifImages = ResourceImages.exifs

        binding.pager.apply {
            setBackgroundColor(Color.BLACK)
            offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            adapter = AssemblyFragmentStateAdapter(
                fragment = this@ExifOrientationTestFragment,
                itemFactoryList = listOf(newPhotoDetailItemFactory(appSettings)),
                initDataList = exifImages.map { Photo(it.uri) }
            )
        }

        TabLayoutMediator(
            binding.tabLayout,
            binding.pager
        ) { tab, position ->
            tab.text = exifImages[position].name
        }.attach()
    }

    private fun newPhotoDetailItemFactory(appSettings: AppSettings): FragmentItemFactory<Photo> {
        return when (val imageLoaderName = appSettings.viewImageLoader.value) {
            "Sketch" -> SketchZoomImageViewFragment.ItemFactory()
            "Coil" -> CoilZoomImageViewFragment.ItemFactory()
            "Glide" -> GlideZoomImageViewFragment.ItemFactory()
            "Picasso" -> PicassoZoomImageViewFragment.ItemFactory()
            "Basic" -> BasicZoomImageViewFragment.ItemFactory()
            else -> throw IllegalArgumentException("Unknown imageLoaderName: $imageLoaderName")
        }
    }
}