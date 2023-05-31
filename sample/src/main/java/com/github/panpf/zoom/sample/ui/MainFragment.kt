package com.github.panpf.zoom.sample.ui

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.panpf.assemblyadapter.recycler.AssemblyRecyclerAdapter
import com.github.panpf.zoom.sample.NavMainDirections
import com.github.panpf.zoom.sample.databinding.MainFragmentBinding
import com.github.panpf.zoom.sample.ui.base.ToolbarBindingFragment
import com.github.panpf.zoom.sample.ui.common.ListSeparator
import com.github.panpf.zoom.sample.ui.common.ListSeparatorItemFactory
import com.github.panpf.zoom.sample.ui.link.Link
import com.github.panpf.zoom.sample.ui.link.LinkItemFactory

class MainFragment : ToolbarBindingFragment<MainFragmentBinding>() {

    private var pendingStartLink: Link? = null
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantedMap ->
            val pendingStartLink = pendingStartLink ?: return@registerForActivityResult
            this@MainFragment.pendingStartLink = null
            requestLinkPermissionsResult(grantedMap, pendingStartLink)
        }

    override fun onViewCreated(
        toolbar: Toolbar,
        binding: MainFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        binding.mainRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = AssemblyRecyclerAdapter(
                listOf(
                    LinkItemFactory().setOnItemClickListener { _, _, _, _, data ->
                        startLink(data)
                    },
                    ListSeparatorItemFactory()
                ),
                listOf(
                    ListSeparator("Compose"),
                    Link(
                        title = "My ZoomImage",
                        navDirections = NavMainDirections.actionGlobalZoomImageFragment(),
                        minSdk = 21
                    ),
                    Link(
                        title = "Telephoto ZoomImage",
                        navDirections = NavMainDirections.actionGlobalTelephotoZoomImageFragment(),
                        minSdk = 21
                    ),

                    ListSeparator("View"),
                    Link(
                        title = "My ZoomImageView",
                        navDirections = NavMainDirections.actionGlobalMyZoomImageViewFragment(),
                    ),
                    // todo subsampingImageView,
                    // todo photoView,
                    // todo ZoomLayout ZoomImageView
                )
            )
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