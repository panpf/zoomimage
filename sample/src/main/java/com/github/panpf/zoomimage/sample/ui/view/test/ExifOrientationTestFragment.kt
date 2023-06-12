package com.github.panpf.zoomimage.sample.ui.view.test

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.github.panpf.assemblyadapter.pager2.AssemblyFragmentStateAdapter
import com.github.panpf.zoomimage.sample.databinding.TabPagerFragmentBinding
import com.github.panpf.zoomimage.sample.ui.view.base.ToolbarBindingFragment
import com.github.panpf.zoomimage.sample.ui.view.zoomimage.SketchZoomImageViewFragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class ExifOrientationTestFragment : ToolbarBindingFragment<TabPagerFragmentBinding>() {

    private val viewModel by viewModels<ExifOrientationTestViewModel>()

    override fun onViewCreated(
        toolbar: Toolbar, binding: TabPagerFragmentBinding, savedInstanceState: Bundle?
    ) {
        toolbar.title = "ExifOrientation Test"

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.showContentState.collect { sampleImages ->
                    binding.tabPagerPager.apply {
                        orientation = ViewPager2.ORIENTATION_HORIZONTAL
                        adapter = AssemblyFragmentStateAdapter(
                            fragment = this@ExifOrientationTestFragment,
                            itemFactoryList = listOf(SketchZoomImageViewFragment.ItemFactory()),
                            initDataList = sampleImages.map { it.second.toUri().toString() }
                        )
                    }
                    TabLayoutMediator(
                        binding.tabPagerTabLayout,
                        binding.tabPagerPager
                    ) { tab, position ->
                        tab.text = sampleImages[position].first
                    }.attach()
                }
            }
        }
    }
}