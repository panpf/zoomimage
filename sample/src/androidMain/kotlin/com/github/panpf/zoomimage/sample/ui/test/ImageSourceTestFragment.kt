package com.github.panpf.zoomimage.sample.ui.test

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.github.panpf.assemblyadapter.pager2.AssemblyFragmentStateAdapter
import com.github.panpf.zoomimage.sample.databinding.FragmentTabPagerBinding
import com.github.panpf.zoomimage.sample.ui.base.BaseToolbarBindingFragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class ImageSourceTestFragment : BaseToolbarBindingFragment<FragmentTabPagerBinding>() {

    override fun onViewCreated(
        toolbar: Toolbar, binding: FragmentTabPagerBinding, savedInstanceState: Bundle?
    ) {
        toolbar.title = "ImageSource"

        viewLifecycleOwner.lifecycleScope.launch {
            val testItems = getImageSourceTestItems(requireContext())

            binding.pager.apply {
                setBackgroundColor(Color.BLACK)
                offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
                orientation = ViewPager2.ORIENTATION_HORIZONTAL
                adapter = AssemblyFragmentStateAdapter(
                    fragment = this@ImageSourceTestFragment,
                    itemFactoryList = listOf(ImageSourceFragment.ItemFactory()),
                    initDataList = testItems.map { it.second }
                )
            }

            TabLayoutMediator(
                binding.tabLayout,
                binding.pager
            ) { tab, position ->
                tab.text = testItems[position].first
            }.attach()
        }

    }
}