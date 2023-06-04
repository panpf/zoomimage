package com.github.panpf.zoomimage.sample.ui.test

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.databinding.ContainerFragmentBinding
import com.github.panpf.zoomimage.sample.ui.base.ToolbarBindingFragment

class OrientationTestFragment : ToolbarBindingFragment<ContainerFragmentBinding>() {

    private val viewModel by viewModels<OrientationTestViewModel>()

    override fun onViewCreated(
        toolbar: Toolbar,
        binding: ContainerFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        toolbar.title = "Orientation Test"

        toolbar.menu.add("Layout").apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            setOnMenuItemClickListener {
                viewModel.changeTab(!viewModel.rowLayoutCheckedData.value!!)
                true
            }
            viewModel.rowLayoutCheckedData.observe(viewLifecycleOwner) {
                val meuIcon = if (viewModel.rowLayoutCheckedData.value!!)
                    R.drawable.ic_layout_column else R.drawable.ic_layout_row
                setIcon(meuIcon)
            }
        }

        viewModel.rowLayoutCheckedData.observe(viewLifecycleOwner) {
            val fragment = if (it!!) {
                ZoomImageViewHorPagerFragment()
            } else {
                ZoomImageViewVerPagerFragment()
            }
            childFragmentManager.beginTransaction()
                .replace(binding.containerFragmentContainer.id, fragment)
                .commit()
        }
    }
}