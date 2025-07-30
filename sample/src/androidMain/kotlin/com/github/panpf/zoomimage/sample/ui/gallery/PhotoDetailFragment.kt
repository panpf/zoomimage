package com.github.panpf.zoomimage.sample.ui.gallery

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.zoomimage.sample.databinding.FragmentContainerBinding
import com.github.panpf.zoomimage.sample.ui.base.BaseBindingFragment
import com.github.panpf.zoomimage.sample.ui.examples.BasicZoomImageViewFragment
import com.github.panpf.zoomimage.sample.ui.examples.BasicZoomImageViewFragmentArgs
import com.github.panpf.zoomimage.sample.ui.examples.CoilZoomImageViewFragment
import com.github.panpf.zoomimage.sample.ui.examples.CoilZoomImageViewFragmentArgs
import com.github.panpf.zoomimage.sample.ui.examples.GlideZoomImageViewFragment
import com.github.panpf.zoomimage.sample.ui.examples.GlideZoomImageViewFragmentArgs
import com.github.panpf.zoomimage.sample.ui.examples.PicassoZoomImageViewFragment
import com.github.panpf.zoomimage.sample.ui.examples.PicassoZoomImageViewFragmentArgs
import com.github.panpf.zoomimage.sample.ui.examples.SketchZoomImageViewFragment
import com.github.panpf.zoomimage.sample.ui.examples.SketchZoomImageViewFragmentArgs
import com.github.panpf.zoomimage.sample.ui.model.Photo
import kotlinx.coroutines.launch

class PhotoDetailFragment : BaseBindingFragment<FragmentContainerBinding>() {

    private val args by navArgs<PhotoDetailFragmentArgs>()

    override fun onViewCreated(
        binding: FragmentContainerBinding,
        savedInstanceState: Bundle?
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            appSettings.viewImageLoader.collect {
                val fragment = newPhotoDetailFragment(args.imageUri, args.placeholderImageUri)
                childFragmentManager.beginTransaction()
                    .replace(binding.fragmentContainer.id, fragment)
                    .commitAllowingStateLoss()
            }
        }
    }

    private fun newPhotoDetailFragment(imageUri: String, placeholderImageUri: String?): Fragment {
        return when (val imageLoaderName = appSettings.viewImageLoader.value) {
            "Sketch" -> SketchZoomImageViewFragment().apply {
                arguments = SketchZoomImageViewFragmentArgs(
                    imageUri = imageUri,
                    placeholderImageUri = placeholderImageUri
                ).toBundle()
            }

            "Coil" -> CoilZoomImageViewFragment().apply {
                arguments = CoilZoomImageViewFragmentArgs(
                    imageUri = imageUri,
                    placeholderImageUri = placeholderImageUri
                ).toBundle()
            }

            "Glide" -> GlideZoomImageViewFragment().apply {
                arguments = GlideZoomImageViewFragmentArgs(imageUri).toBundle()
            }

            "Picasso" -> PicassoZoomImageViewFragment().apply {
                arguments = PicassoZoomImageViewFragmentArgs(imageUri).toBundle()
            }

            "Basic" -> BasicZoomImageViewFragment().apply {
                arguments = BasicZoomImageViewFragmentArgs(imageUri).toBundle()
            }

            else -> throw IllegalArgumentException("Unknown imageLoaderName: $imageLoaderName")
        }
    }

    class ItemFactory : FragmentItemFactory<Photo>(Photo::class) {

        override fun createFragment(
            bindingAdapterPosition: Int,
            absoluteAdapterPosition: Int,
            data: Photo
        ): Fragment = PhotoDetailFragment().apply {
            arguments = PhotoDetailFragmentArgs(
                imageUri = data.originalUrl,
                placeholderImageUri = data.listThumbnailUrl
            ).toBundle()
        }
    }
}