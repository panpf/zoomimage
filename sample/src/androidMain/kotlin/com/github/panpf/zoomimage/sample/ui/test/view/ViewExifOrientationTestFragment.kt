package com.github.panpf.zoomimage.sample.ui.test.view

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.github.panpf.assemblyadapter.pager2.AssemblyFragmentStateAdapter
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.databinding.FragmentTabPagerBinding
import com.github.panpf.zoomimage.sample.ui.base.view.BaseToolbarBindingFragment
import com.github.panpf.zoomimage.sample.ui.examples.view.ZoomImageViewFragment
import com.github.panpf.zoomimage.sample.ui.examples.view.ZoomImageViewOptionsDialogFragment
import com.github.panpf.zoomimage.sample.ui.examples.view.ZoomImageViewOptionsDialogFragmentArgs
import com.github.panpf.zoomimage.sample.ui.examples.view.ZoomViewType
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class ViewExifOrientationTestFragment : BaseToolbarBindingFragment<FragmentTabPagerBinding>() {

    private val exifOrientationTestContentViewModel by viewModels<ExifOrientationTestContentViewModel>()

    override fun onViewCreated(
        toolbar: Toolbar, binding: FragmentTabPagerBinding, savedInstanceState: Bundle?
    ) {
        toolbar.title = "ExifOrientation Test"
        toolbar.subtitle = "View"

        toolbar.menu.add("Options").apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            setOnMenuItemClickListener {
                ZoomImageViewOptionsDialogFragment().apply {
                    arguments = ZoomImageViewOptionsDialogFragmentArgs(
                        zoomViewType = ZoomViewType.ZoomImageView.name,
                    ).toBundle()
                }.show(childFragmentManager, null)
                true
            }
            setIcon(R.drawable.ic_settings)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                exifOrientationTestContentViewModel.showContentState.collect { sampleImages ->
                    binding.pager.apply {
                        offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
                        orientation = ViewPager2.ORIENTATION_HORIZONTAL
                        adapter = AssemblyFragmentStateAdapter(
                            fragment = this@ViewExifOrientationTestFragment,
                            itemFactoryList = listOf(ZoomImageViewFragment.ItemFactory()),
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