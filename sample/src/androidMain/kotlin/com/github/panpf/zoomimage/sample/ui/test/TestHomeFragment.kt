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
import com.github.panpf.zoomimage.sample.databinding.FragmentRecyclerBinding
import com.github.panpf.zoomimage.sample.ui.base.view.BaseBindingFragment
import com.github.panpf.zoomimage.sample.ui.common.view.list.GridSeparatorItemFactory
import com.github.panpf.zoomimage.sample.ui.common.view.list.LinkItemFactory
import com.github.panpf.zoomimage.sample.ui.model.Link

class TestHomeFragment : BaseBindingFragment<FragmentRecyclerBinding>() {

    private var pendingStartLink: Link? = null
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantedMap ->
            val pendingStartLink = pendingStartLink ?: return@registerForActivityResult
            this@TestHomeFragment.pendingStartLink = null
            requestLinkPermissionsResult(grantedMap, pendingStartLink)
        }

    override fun onViewCreated(
        binding: FragmentRecyclerBinding,
        savedInstanceState: Bundle?
    ) {
        binding.recycler.apply {
            setPadding(0, 0, 0, 80.dp2px)
            clipToPadding = false

            layoutManager = AssemblyGridLayoutManager.Builder(requireContext(), 2).apply {
                itemSpanByItemFactory(
                    ProjectInfoItemFactory::class to ItemSpan.fullSpan(),
                    GridSeparatorItemFactory::class to ItemSpan.fullSpan(),
                )
            }.build()
            adapter = AssemblyRecyclerAdapter(
                itemFactoryList = listOf(
                    LinkItemFactory().setOnItemClickListener { _, _, _, _, data ->
                        startLink(data)
                    },
                    ProjectInfoItemFactory(),
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
        // TODO test items
//        Link(
//            title = "AnimatablePlaceholder",
//            navDirections = NavMainDirections.actionAnimatablePlaceholderTestViewFragment(),
//        ),
//        Link(
//            title = "DisplayInsanity",
//            navDirections = NavMainDirections.actionInsanityTestViewFragment()
//        ),
//        Link(
//            title = "Decoder",
//            navDirections = NavMainDirections.actionDecoderTestPagerFragment()
//        ),
//        Link(
//            title = "ExifOrientation",
//            navDirections = NavMainDirections.actionExifOrientationTestPagerFragment()
//        ),
//        Link(
//            title = "Fetcher",
//            navDirections = NavMainDirections.actionFetcherTestFragment(),
//            permissions = listOf(permission.READ_EXTERNAL_STORAGE)
//        ),
//        Link(
//            title = "Local Videos",
//            navDirections = NavMainDirections.actionLocalVideoListFragment(),
//            permissions = listOf(permission.READ_EXTERNAL_STORAGE)
//        ),
//        Link(
//            title = "ProgressIndicator",
//            navDirections = NavMainDirections.actionProgressIndicatorTestViewFragment()
//        ),
//        Link(
//            title = "RemoteViews",
//            navDirections = NavMainDirections.actionRemoteViewsFragment()
//        ),
////        Link(
////            title = "ShareElement",
////            navDirections = NavMainDirections.actionShareElementTestFragment(),
////        ),
//        Link(
//            title = "Transformation",
//            navDirections = NavMainDirections.actionTransformationTestPagerFragment()
//        ),
//        Link(
//            title = "Temp",
//            navDirections = NavMainDirections.actionTempTestFragment()
//        ),
        "ProjectInfo"
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