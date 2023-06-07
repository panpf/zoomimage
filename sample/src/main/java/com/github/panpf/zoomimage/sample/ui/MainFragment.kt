package com.github.panpf.zoomimage.sample.ui

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.panpf.assemblyadapter.recycler.AssemblyRecyclerAdapter
import com.github.panpf.zoomimage.sample.NavMainDirections
import com.github.panpf.zoomimage.sample.databinding.MainFragmentBinding
import com.github.panpf.zoomimage.sample.ui.base.ToolbarBindingFragment
import com.github.panpf.zoomimage.sample.ui.common.list.ListSeparator
import com.github.panpf.zoomimage.sample.ui.common.list.ListSeparatorItemFactory
import com.github.panpf.zoomimage.sample.ui.link.Link
import com.github.panpf.zoomimage.sample.ui.link.LinkItemFactory

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
                        title = "ZoomImage（My）",
                        navDirections = NavMainDirections.actionGlobalMyZoomImageFragment(),
                        minSdk = 21
                    ),
                    Link(
                        title = "ZoomableAsyncImage（Telephoto）",
                        navDirections = NavMainDirections.actionGlobalTelephotoZoomImageFragment(),
                        minSdk = 21
                    ),

                    ListSeparator("View"),
                    Link(
                        title = "ZoomImageView（My）",
                        navDirections = NavMainDirections.actionGlobalZoomImageViewPagerFragment(),
                    ),
                    Link(
                        title = "SketchZoomImageView（My）",
                        navDirections = NavMainDirections.actionGlobalSketchZoomImageViewPagerFragment(),
                    ),
                    Link(
                        title = "CoilZoomImageView（My）",
                        navDirections = NavMainDirections.actionGlobalCoilZoomImageViewPagerFragment(),
                        minSdk = 21
                    ),
                    Link(
                        title = "GlideZoomImageView（My）",
                        navDirections = NavMainDirections.actionGlobalGlideZoomImageViewPagerFragment(),
                    ),
                    Link(
                        title = "PicassoZoomImageView（My）",
                        navDirections = NavMainDirections.actionGlobalPicassoZoomImageViewPagerFragment(),
                    ),
                    Link(
                        title = "PhotoView",
                        navDirections = NavMainDirections.actionGlobalPhotoViewPagerFragment(),
                    ),
                    Link(
                        title = "SubsamplingScaleImageView",
                        navDirections = NavMainDirections.actionGlobalSubsamplingViewPagerFragment(),
                    ),

                    ListSeparator("Test"),
                    Link(
                        title = "ZoomImageView Orientation Test",
                        navDirections = NavMainDirections.actionGlobalOrientationTestFragment(),
                    ),
                    // todo exifOrientation
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