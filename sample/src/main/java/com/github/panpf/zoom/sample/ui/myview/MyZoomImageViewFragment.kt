package com.github.panpf.zoom.sample.ui.myview

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import com.github.panpf.zoom.sample.R
import com.github.panpf.zoom.sample.databinding.ContainerFragmentBinding
import com.github.panpf.zoom.sample.eventService
import com.github.panpf.zoom.sample.ui.base.ToolbarBindingFragment

class MyZoomImageViewFragment : ToolbarBindingFragment<ContainerFragmentBinding>() {

    private val viewModel by viewModels<HugeImageListViewModel>()

    override fun onViewCreated(
        toolbar: Toolbar,
        binding: ContainerFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        toolbar.title = "Huge Image"

        toolbar.menu.add("Rotate").apply {
            setIcon(R.drawable.ic_rotate_right)
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            setOnMenuItemClickListener {
                eventService.hugeViewerPageRotateEvent.value = 0
                true
            }
        }

        val menu = toolbar.menu.add("Layout").apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            setOnMenuItemClickListener {
                val newLayout = if (viewModel.layoutData.value == Layout.COLUMN) {
                    Layout.ROW
                } else {
                    Layout.COLUMN
                }
                viewModel.changeTab(newLayout)
                true
            }
        }

        viewModel.layoutData.observe(viewLifecycleOwner) {
            val meuIcon = if (it == Layout.COLUMN) {
                R.drawable.ic_layout_row
            } else {
                R.drawable.ic_layout_column
            }
            menu.setIcon(meuIcon)

            val fragment = if (it == Layout.COLUMN) {
                HugeImageHorPagerFragment()
            } else {
                HugeImageVerPagerFragment()
            }
            childFragmentManager.beginTransaction()
                .replace(binding.containerFragmentContainer.id, fragment)
                .commit()
        }
//
//        toolbar.menu.add("Settings").apply {
//            setIcon(R.drawable.ic_settings)
//            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
//            setOnMenuItemClickListener {
//                findNavController().navigate(
//                    MainFragmentDirections.actionGlobalSettingsDialogFragment(Page.ZOOM.name)
//                )
//                true
//            }
//        }
    }
}