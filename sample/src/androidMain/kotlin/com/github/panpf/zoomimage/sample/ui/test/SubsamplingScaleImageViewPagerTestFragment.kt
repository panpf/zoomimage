package com.github.panpf.zoomimage.sample.ui.test

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.assemblyadapter.pager2.AssemblyFragmentStateAdapter
import com.github.panpf.zoomimage.sample.databinding.FragmentTabPagerBinding
import com.github.panpf.zoomimage.sample.ui.base.BaseToolbarBindingFragment
import com.google.android.material.tabs.TabLayoutMediator

class SubsamplingScaleImageViewPagerTestFragment :
    BaseToolbarBindingFragment<FragmentTabPagerBinding>() {

    override fun getNavigationBarInsetsView(binding: FragmentTabPagerBinding): View {
        return binding.root
    }

    override fun onViewCreated(
        toolbar: Toolbar, binding: FragmentTabPagerBinding, savedInstanceState: Bundle?
    ) {
        toolbar.title = "SubsamplingScaleImageView (Pager)"

        val images = ResourceImages.values

        binding.pager.apply {
            setBackgroundColor(Color.BLACK)
            offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            adapter = AssemblyFragmentStateAdapter(
                fragment = this@SubsamplingScaleImageViewPagerTestFragment,
                itemFactoryList = listOf(SubsamplingScaleImageViewFragment.ItemFactory()),
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