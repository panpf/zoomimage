package com.github.panpf.zoomimage.sample.ui.test.view

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.githb.panpf.zoomimage.images.Images
import com.github.panpf.sketch.loadImage
import com.github.panpf.zoomimage.sample.databinding.FragmentTestZoomViewBinding
import com.github.panpf.zoomimage.sample.ui.base.view.BaseToolbarBindingFragment

class ZoomImageViewTestFragment : BaseToolbarBindingFragment<FragmentTestZoomViewBinding>() {

    override fun onViewCreated(
        toolbar: Toolbar,
        binding: FragmentTestZoomViewBinding,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(toolbar, binding, savedInstanceState)
        toolbar.title = "ZoomImageView"

        binding.zoomImageView.loadImage(Images.hugeChina.uri)
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