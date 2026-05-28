package com.github.panpf.zoomimage.sample.ui.test

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.github.panpf.assemblyadapter.recycler.AssemblyGridLayoutManager
import com.github.panpf.assemblyadapter.recycler.AssemblyRecyclerAdapter
import com.github.panpf.assemblyadapter.recycler.ItemSpan
import com.github.panpf.assemblyadapter.recycler.divider.AssemblyGridDividerItemDecoration
import com.github.panpf.assemblyadapter.recycler.divider.Divider
import com.github.panpf.tools4a.dimen.ktx.dp2px
import com.github.panpf.zoomimage.sample.NavMainDirections
import com.github.panpf.zoomimage.sample.databinding.FragmentRecyclerBinding
import com.github.panpf.zoomimage.sample.ui.base.BaseBindingFragment
import com.github.panpf.zoomimage.sample.ui.common.list.ProjectInfoItemFactory
import com.github.panpf.zoomimage.sample.ui.common.list.TestGroupItemFactory
import com.github.panpf.zoomimage.sample.ui.common.list.TestItemItemFactory
import com.github.panpf.zoomimage.sample.ui.model.ViewTestGroup
import com.github.panpf.zoomimage.sample.ui.model.ViewTestItem

class TestHomeFragment : BaseBindingFragment<FragmentRecyclerBinding>() {

    override fun onViewCreated(
        binding: FragmentRecyclerBinding,
        savedInstanceState: Bundle?
    ) {
        binding.recycler.apply {
            layoutManager = AssemblyGridLayoutManager.Builder(requireContext(), 2).apply {
                itemSpanByItemFactory(
                    TestGroupItemFactory::class to ItemSpan.fullSpan(),
                    ProjectInfoItemFactory::class to ItemSpan.fullSpan(),
                )
            }.build()
            adapter = AssemblyRecyclerAdapter(
                itemFactoryList = listOf(
                    TestItemItemFactory()
                        .setOnItemClickListener { _, _, _, _, data ->
                            this@TestHomeFragment.findNavController().navigate(data.navDirections)
                        },
                    TestGroupItemFactory(),
                    ProjectInfoItemFactory(),
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
        ViewTestGroup("Functions"),
        ViewTestItem(
            title = "ImageSource",
            navDirections = NavMainDirections.actionGlobalImageSourceTestFragment(),
        ),
        ViewTestItem(
            title = "Exif Orientation",
            navDirections = NavMainDirections.actionGlobalExifOrientationTestFragment(),
        ),

        ViewTestGroup("UI"),
        ViewTestItem(
            title = "Overlay",
            navDirections = NavMainDirections.actionGlobalOverlayTestFragment(),
        ),
        ViewTestItem(
            title = "Image Matrix",
            navDirections = NavMainDirections.actionGlobalImageMatrixFragment(),
        ),

        ViewTestGroup("Switch"),
        ViewTestItem(
            title = "PhotoView (Switch)",
            navDirections = NavMainDirections.actionGlobalPhotoViewSwitchTestFragment(),
        ),
        ViewTestItem(
            title = "SubsamplingScaleImageView (Switch)",
            navDirections = NavMainDirections.actionGlobalSubsamplingScaleImageViewSwitchTestFragment(),
        ),
        ViewTestItem(
            title = "ZoomImageView (Switch)",
            navDirections = NavMainDirections.actionGlobalZoomImageViewSwitchTestFragment(),
        ),

        ViewTestGroup("Pager"),
        ViewTestItem(
            title = "PhotoView (Pager)",
            navDirections = NavMainDirections.actionGlobalPhotoViewPagerTestFragment(),
        ),
        ViewTestItem(
            title = "SubsamplingScaleImageView (Pager)",
            navDirections = NavMainDirections.actionGlobalSubsamplingScaleImageViewPagerTestFragment(),
        ),

        ViewTestGroup("Other"),
        ViewTestItem(
            title = "Temp",
            navDirections = NavMainDirections.actionGlobalTempTestFragment(),
        ),

        "ProjectInfo"
    )
}