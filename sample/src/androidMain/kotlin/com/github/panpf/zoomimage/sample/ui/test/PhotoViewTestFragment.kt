package com.github.panpf.zoomimage.sample.ui.test

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.assemblyadapter.pager2.AssemblyFragmentStateAdapter
import com.github.panpf.zoomimage.sample.databinding.FragmentTabPagerBinding
import com.github.panpf.zoomimage.sample.ui.base.BaseToolbarBindingFragment
import com.github.panpf.zoomimage.sample.ui.examples.PhotoViewFragment
import com.google.android.material.tabs.TabLayoutMediator

class PhotoViewTestFragment : BaseToolbarBindingFragment<FragmentTabPagerBinding>() {

    override fun onViewCreated(
        toolbar: Toolbar, binding: FragmentTabPagerBinding, savedInstanceState: Bundle?
    ) {
        toolbar.title = "PhotoView"

        val images = ResourceImages.values

        binding.pager.apply {
            setBackgroundColor(Color.BLACK)
            offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            adapter = AssemblyFragmentStateAdapter(
                fragment = this@PhotoViewTestFragment,
                itemFactoryList = listOf(PhotoViewFragment.ItemFactory()),
                initDataList = images.map { it.uri }
            )
        }
        TabLayoutMediator(
            binding.tabLayout,
            binding.pager
        ) { tab, position ->
            tab.text = images[position].name
        }.attach()
    }
}