package com.github.panpf.zoomimage.sample.ui.test.view

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.github.panpf.assemblyadapter.pager2.AssemblyFragmentStateAdapter
import com.github.panpf.zoomimage.sample.databinding.FragmentTabPagerBinding
import com.github.panpf.zoomimage.sample.ui.base.view.BaseToolbarBindingFragment
import com.github.panpf.zoomimage.sample.ui.examples.view.BasicZoomImageViewFragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class ExifOrientationTestFragment : BaseToolbarBindingFragment<FragmentTabPagerBinding>() {

    private val exifOrientationTestContentViewModel by viewModels<ExifOrientationTestContentViewModel>()

    override fun onViewCreated(
        toolbar: Toolbar, binding: FragmentTabPagerBinding, savedInstanceState: Bundle?
    ) {
        toolbar.title = "ExifOrientation"

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                exifOrientationTestContentViewModel.showContentState.collect { sampleImages ->
                    binding.pager.apply {
                        offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
                        orientation = ViewPager2.ORIENTATION_HORIZONTAL
                        adapter = AssemblyFragmentStateAdapter(
                            fragment = this@ExifOrientationTestFragment,
                            itemFactoryList = listOf(BasicZoomImageViewFragment.ItemFactory()),
                            initDataList = sampleImages.map { it.second }
                        )
                    }
                    TabLayoutMediator(
                        binding.tabLayout,
                        binding.pager
                    ) { tab, position ->
                        tab.text = sampleImages[position].first
                    }.attach()
                }
            }
        }
    }
}