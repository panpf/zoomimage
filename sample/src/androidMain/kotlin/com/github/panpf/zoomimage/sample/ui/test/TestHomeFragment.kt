package com.github.panpf.zoomimage.sample.ui.test

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.github.panpf.assemblyadapter.recycler.AssemblyGridLayoutManager
import com.github.panpf.assemblyadapter.recycler.AssemblyRecyclerAdapter
import com.github.panpf.assemblyadapter.recycler.ItemSpan
import com.github.panpf.assemblyadapter.recycler.divider.AssemblyGridDividerItemDecoration
import com.github.panpf.assemblyadapter.recycler.divider.Divider
import com.github.panpf.tools4a.dimen.ktx.dp2px
import com.github.panpf.zoomimage.sample.NavMainDirections
import com.github.panpf.zoomimage.sample.databinding.FragmentTestHomeBinding
import com.github.panpf.zoomimage.sample.ui.base.BaseBindingFragment
import com.github.panpf.zoomimage.sample.ui.common.list.GridSeparatorItemFactory
import com.github.panpf.zoomimage.sample.ui.common.list.LinkItemFactory
import com.github.panpf.zoomimage.sample.ui.model.Link

class TestHomeFragment : BaseBindingFragment<FragmentTestHomeBinding>() {

    private var pendingStartLink: Link? = null
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantedMap ->
            val pendingStartLink = pendingStartLink ?: return@registerForActivityResult
            this@TestHomeFragment.pendingStartLink = null
            requestLinkPermissionsResult(grantedMap, pendingStartLink)
        }

    override fun onViewCreated(
        binding: FragmentTestHomeBinding,
        savedInstanceState: Bundle?
    ) {
        binding.recycler.apply {
            setPadding(0, 0, 0, 80.dp2px)
            clipToPadding = false

            layoutManager = AssemblyGridLayoutManager.Builder(requireContext(), 2).apply {
                itemSpanByItemFactory(
                    GridSeparatorItemFactory::class to ItemSpan.fullSpan(),
                )
            }.build()
            adapter = AssemblyRecyclerAdapter(
                itemFactoryList = listOf(
                    LinkItemFactory().setOnItemClickListener { _, _, _, _, data ->
                        startLink(data)
                    },
                    GridSeparatorItemFactory(),
                ),
                initDataList = pageList()
            )
            addItemDecoration(AssemblyGridDividerItemDecoration.Builder(requireContext()).apply {
                divider(Divider.space(16.dp2px))
                sideDivider(Divider.space(16.dp2px))
                useDividerAsHeaderAndFooterDivider()
                useSideDividerAsSideHeaderAndFooterDivider()
            }.build())
        }
    }

    private fun pageList(): List<Any> = listOf(
        Link(
            title = "ImageSource",
            navDirections = NavMainDirections.actionGlobalImageSourceTestFragment(),
            minSdk = 21
        ),
        Link(
            title = "Exif Orientation",
            navDirections = NavMainDirections.actionGlobalExifOrientationTestFragment(),
            minSdk = 21
        ),
        Link(
            title = "Image Matrix",
            navDirections = NavMainDirections.actionGlobalImageMatrixFragment(),
        ),
        Link(
            title = "PhotoView",
            navDirections = NavMainDirections.actionGlobalPhotoViewTestFragment(),
            minSdk = 21,
        ),
        Link(
            title = "SubsamplingScaleImageView",
            navDirections = NavMainDirections.actionGlobalSubsamplingScaleImageViewTestFragment(),
            minSdk = 21,
        ),
    )

    private fun startLink(data: Link) {
        if (data.minSdk == null || Build.VERSION.SDK_INT >= data.minSdk) {
            val permissions = data.permissions
            if (permissions != null) {
                pendingStartLink = data
                permissionLauncher.launch(permissions.toTypedArray())
            } else {
                data.navDirections?.let { findNavController().navigate(it) }
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
            data.navDirections?.let { findNavController().navigate(it) }
        } else {
            Toast.makeText(context, "Please grant permission", Toast.LENGTH_LONG).show()
        }
    }
}