package com.github.panpf.zoomimage.sample.ui.test.view

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.github.panpf.sketch.displayImage
import com.github.panpf.zoomimage.sample.SampleImages
import com.github.panpf.zoomimage.sample.databinding.ZoomImageViewTestFragmentBinding
import com.github.panpf.zoomimage.sample.ui.base.view.ToolbarBindingFragment

class ZoomImageViewTestFragment : ToolbarBindingFragment<ZoomImageViewTestFragmentBinding>() {

    override fun onViewCreated(
        toolbar: Toolbar,
        binding: ZoomImageViewTestFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(toolbar, binding, savedInstanceState)
        toolbar.title = "ZoomImageView"

        binding.zoomImageView.displayImage(SampleImages.Asset.WORLD.uri)
    }
}

//class ZoomImageViewTestFragment : ToolbarBindingFragment<ContainerFragmentBinding>() {
//
//    override fun onViewCreated(
//        toolbar: Toolbar,
//        binding: ContainerFragmentBinding,
//        savedInstanceState: Bundle?
//    ) {
//        super.onViewCreated(toolbar, binding, savedInstanceState)
//        toolbar.title = "Image Matrix"
//
//        val fragment = ZoomImageViewFragment().apply {
//            arguments = ZoomImageViewFragmentArgs(imageUri = SampleImages.Asset.DOG.uri).toBundle()
//        }
//        childFragmentManager.beginTransaction()
//            .replace(binding.containerFragmentContainer.id, fragment)
//            .commit()
//    }
//}