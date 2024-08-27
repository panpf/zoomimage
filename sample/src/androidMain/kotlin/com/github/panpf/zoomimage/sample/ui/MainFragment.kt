package com.github.panpf.zoomimage.sample.ui

import android.os.Bundle
import com.github.panpf.zoomimage.sample.appSettings
import com.github.panpf.zoomimage.sample.databinding.FragmentContainerBinding
import com.github.panpf.zoomimage.sample.ui.base.BaseBindingFragment
import com.github.panpf.zoomimage.sample.util.collectWithLifecycle

class MainFragment : BaseBindingFragment<FragmentContainerBinding>() {

    override fun onViewCreated(
        binding: FragmentContainerBinding,
        savedInstanceState: Bundle?
    ) {
        appSettings.composePage.collectWithLifecycle(viewLifecycleOwner) {
            val fragment = if (it) ComposeHomeFragment() else ViewNavHostFragment()
            childFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, fragment)
                .commit()
        }
    }
}