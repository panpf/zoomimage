package com.github.panpf.zoomimage.sample.ui.test

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.github.panpf.assemblyadapter.pager2.AssemblyFragmentStateAdapter
import com.github.panpf.zoomimage.images.ComposeResImageFiles
import com.github.panpf.zoomimage.sample.databinding.FragmentTabPagerBinding
import com.github.panpf.zoomimage.sample.ui.base.BaseToolbarBindingFragment
import com.google.android.material.tabs.TabLayoutMediator

class ExifOrientationTestFragment : BaseToolbarBindingFragment<FragmentTabPagerBinding>() {

    override fun onViewCreated(
        toolbar: Toolbar, binding: FragmentTabPagerBinding, savedInstanceState: Bundle?
    ) {
        toolbar.title = "Exif Orientation"

        val exifImages = ComposeResImageFiles.exifs
        val dataList = exifImages.map { it.uri }
        val tabTitles = exifImages.map {
            it.name.substring(0, it.name.indexOf(".")).uppercase()
        }

        binding.pager.apply {
            offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            adapter = AssemblyFragmentStateAdapter(
                fragment = this@ExifOrientationTestFragment,
                itemFactoryList = listOf(SimpleZoomImageViewFragment.ItemFactory()),
                initDataList = dataList
            )
        }

        TabLayoutMediator(
            binding.tabLayout,
            binding.pager
        ) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }
}