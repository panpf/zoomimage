package com.github.panpf.zoomimage.sample.ui

import android.os.Bundle
import com.github.panpf.zoomimage.sample.appSettings
import com.github.panpf.zoomimage.sample.databinding.FragmentContainerBinding
import com.github.panpf.zoomimage.sample.ui.base.view.BaseBindingFragment
import com.github.panpf.zoomimage.sample.ui.gallery.ComposeHomeFragment
import com.github.panpf.zoomimage.sample.ui.util.collectWithLifecycle

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

//class MainFragment : BaseToolbarBindingFragment<FragmentRecyclerBinding>() {
//
//    private var pendingStartLink: Link? = null
//    private val permissionLauncher =
//        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantedMap ->
//            val pendingStartLink = pendingStartLink ?: return@registerForActivityResult
//            this@MainFragment.pendingStartLink = null
//            requestLinkPermissionsResult(grantedMap, pendingStartLink)
//        }
//    private val viewModel by viewModels<HomeViewModel>()
//
//    override fun onViewCreated(
//        toolbar: Toolbar,
//        binding: FragmentRecyclerBinding,
//        savedInstanceState: Bundle?
//    ) {
//        binding.recycler.apply {
//            layoutManager = LinearLayoutManager(requireContext())
//            adapter = AssemblyRecyclerAdapter<Any>(
//                listOf(
//                    LinkItemFactory().setOnItemClickListener { _, _, _, _, data ->
//                        startLink(data)
//                    },
//                    ListSeparatorItemFactory()
//                ),
//            ).apply {
//                viewLifecycleOwner.lifecycleScope.launch {
//                    repeatOnLifecycle(State.STARTED) {
//                        viewModel.data.collectLatest {
//                            submitList(it)
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private fun startLink(data: Link) {
//        if (data.minSdk == null || Build.VERSION.SDK_INT >= data.minSdk) {
//            val permissions = data.permissions
//            if (permissions != null) {
//                pendingStartLink = data
//                permissionLauncher.launch(permissions.toTypedArray())
//            } else {
//                data.navDirections?.let { findNavController().navigate(it) }
//            }
//        } else {
//            Toast.makeText(
//                context,
//                "Must be API ${data.minSdk} or above",
//                Toast.LENGTH_LONG
//            ).show()
//        }
//    }
//
//    private fun requestLinkPermissionsResult(grantedMap: Map<String, Boolean>, data: Link) {
//        if (grantedMap.values.all { it }) {
//            data.navDirections?.let { findNavController().navigate(it) }
//        } else {
//            Toast.makeText(context, "Please grant permission", Toast.LENGTH_LONG).show()
//        }
//    }
//}