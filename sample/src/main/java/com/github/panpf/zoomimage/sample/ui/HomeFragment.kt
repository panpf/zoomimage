package com.github.panpf.zoomimage.sample.ui

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.panpf.assemblyadapter.recycler.AssemblyRecyclerAdapter
import com.github.panpf.zoomimage.sample.databinding.HomeFragmentBinding
import com.github.panpf.zoomimage.sample.ui.base.view.ToolbarBindingFragment
import com.github.panpf.zoomimage.sample.ui.common.view.list.LinkItemFactory
import com.github.panpf.zoomimage.sample.ui.common.view.list.ListSeparatorItemFactory
import com.github.panpf.zoomimage.sample.ui.model.Link
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : ToolbarBindingFragment<HomeFragmentBinding>() {

    private var pendingStartLink: Link? = null
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantedMap ->
            val pendingStartLink = pendingStartLink ?: return@registerForActivityResult
            this@HomeFragment.pendingStartLink = null
            requestLinkPermissionsResult(grantedMap, pendingStartLink)
        }
    private val viewModel by viewModels<HomeViewModel>()

    override fun onViewCreated(
        toolbar: Toolbar,
        binding: HomeFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        binding.homeRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = AssemblyRecyclerAdapter<Any>(
                listOf(
                    LinkItemFactory().setOnItemClickListener { _, _, _, _, data ->
                        startLink(data)
                    },
                    ListSeparatorItemFactory()
                ),
            ).apply {
                viewLifecycleOwner.lifecycleScope.launch {
                    repeatOnLifecycle(State.STARTED) {
                        viewModel.data.collectLatest {
                            submitList(it)
                        }
                    }
                }
            }
        }
    }

    private fun startLink(data: Link) {
        if (data.minSdk == null || Build.VERSION.SDK_INT >= data.minSdk) {
            val permissions = data.permissions
            if (permissions != null) {
                pendingStartLink = data
                permissionLauncher.launch(permissions.toTypedArray())
            } else {
                findNavController().navigate(data.navDirections)
            }
        } else {
            Toast.makeText(
                context,
                "Must be API ${data.minSdk} or above",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun requestLinkPermissionsResult(grantedMap: Map<String, Boolean>, data: Link) {
        if (grantedMap.values.all { it }) {
            findNavController().navigate(data.navDirections)
        } else {
            Toast.makeText(context, "Please grant permission", Toast.LENGTH_LONG).show()
        }
    }
}