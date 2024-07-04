package com.github.panpf.zoomimage.sample.ui

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.github.panpf.zoomimage.sample.appSettings
import com.github.panpf.zoomimage.sample.databinding.FragmentContainerBinding
import com.github.panpf.zoomimage.sample.ui.base.view.BaseBindingFragment
import com.github.panpf.zoomimage.sample.ui.gallery.ComposeHomeFragment
import com.github.panpf.zoomimage.sample.util.collectWithLifecycle

class MainFragment : BaseBindingFragment<FragmentContainerBinding>() {

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            appSettings.composePage.collectWithLifecycle(viewLifecycleOwner) {
                val fragment = if (it) ComposeHomeFragment() else ViewNavHostFragment()
                childFragmentManager.beginTransaction()
                    .replace(binding!!.fragmentContainer.id, fragment)
                    .commit()
            }
        }

    override fun onViewCreated(
        binding: FragmentContainerBinding,
        savedInstanceState: Bundle?
    ) {
        permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}