package com.github.panpf.zoomimage.sample.ui.test

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.github.panpf.assemblyadapter.pager2.AssemblyFragmentStateAdapter
import com.github.panpf.zoomimage.images.AssetImageFiles
import com.github.panpf.zoomimage.sample.databinding.FragmentTabPagerBinding
import com.github.panpf.zoomimage.sample.ui.base.BaseToolbarBindingFragment
import com.google.android.material.tabs.TabLayoutMediator

class PhotoViewPagerTestFragment : BaseToolbarBindingFragment<FragmentTabPagerBinding>() {

    override fun onViewCreated(
        toolbar: Toolbar, binding: FragmentTabPagerBinding, savedInstanceState: Bundle?
    ) {
        toolbar.title = "PhotoView (Pager)"

        val images = arrayOf(
            AssetImageFiles.cat,
            AssetImageFiles.dog,
            AssetImageFiles.longEnd,
            AssetImageFiles.longWhale,
            AssetImageFiles.hugeChina,
            AssetImageFiles.hugeLongComic,
            AssetImageFiles.hugeLongQmsht,
        )
        val dataList = images.map { it.uri }
        val tabTitles = images.map {
            it.name.substring(0, it.name.indexOf(".")).uppercase()
        }

        binding.pager.apply {
            offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            adapter = AssemblyFragmentStateAdapter(
                fragment = this@PhotoViewPagerTestFragment,
                itemFactoryList = listOf(PhotoViewFragment.ItemFactory()),
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