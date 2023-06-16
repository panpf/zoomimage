/*
 * Copyright (C) 2022 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.panpf.zoomimage.sample.ui.view.photoview

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.graphics.PointF
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.github.chrisbanes.photoview.PhotoView
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.sketch.displayImage
import com.github.panpf.zoomimage.sample.databinding.PhotoViewFragmentBinding
import com.github.panpf.zoomimage.sample.ui.view.base.BindingFragment
import com.github.panpf.zoomimage.sample.util.format
import com.github.panpf.zoomimage.sample.util.toShortString
import com.github.panpf.zoomimage.sample.util.toVeryShortString
import kotlin.math.pow
import kotlin.math.sqrt

class PhotoViewFragment : BindingFragment<PhotoViewFragmentBinding>() {

    private val args by navArgs<PhotoViewFragmentArgs>()

    override fun onViewCreated(
        binding: PhotoViewFragmentBinding,
        savedInstanceState: Bundle?
    ) {
//        binding.photoViewUriText.text = "uri: ${args.imageUri}"

        binding.photoView.apply {
            setOnScaleChangeListener { _, _, _ ->
                updateInfo(binding)
            }
            setOnMatrixChangeListener {
                updateInfo(binding)
            }
        }
        updateInfo(binding)

        binding.photoViewErrorRetryButton.setOnClickListener {
            setImage(binding)
        }

        setImage(binding)
    }

    private fun setImage(binding: PhotoViewFragmentBinding) {
        binding.photoView.displayImage(args.imageUri) {
            lifecycle(viewLifecycleOwner.lifecycle)
            crossfade()
            listener(
                onStart = {
                    binding.photoViewProgress.isVisible = true
                    binding.photoViewErrorLayout.isVisible = false
                },
                onSuccess = { _, _ ->
                    binding.photoViewProgress.isVisible = false
                    binding.photoViewErrorLayout.isVisible = false
                },
                onError = { _, _ ->
                    binding.photoViewProgress.isVisible = false
                    binding.photoViewErrorLayout.isVisible = true
                },
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateInfo(binding: PhotoViewFragmentBinding) {
        binding.photoViewInfoHeaderText.text = """
                scale: 
                visible: 
                translation: 
            """.trimIndent()
        binding.photoViewInfoContentText.text = binding.photoView.run {
            val scales = floatArrayOf(minimumScale, mediumScale, maximumScale)
                .joinToString(prefix = "[", postfix = "]") { it.format(2).toString() }
            """
                ${displayScale.format(2)}(${baseScale.format(2)}x${suppScale.format(2)}) in $scales
                ${displayRect?.toVeryShortString()}
                ${imageMatrix?.getTranslation()?.toShortString()}
            """.trimIndent()
        }
    }

    class ItemFactory : FragmentItemFactory<String>(String::class) {

        override fun createFragment(
            bindingAdapterPosition: Int,
            absoluteAdapterPosition: Int,
            data: String
        ): Fragment = PhotoViewFragment().apply {
            arguments = PhotoViewFragmentArgs(data).toBundle()
        }
    }

    private fun Matrix.getTranslation(): PointF {
        val point = PointF()
        val values = FloatArray(9).apply { getValues(this) }
        point.x = values[Matrix.MTRANS_X]
        point.y = values[Matrix.MTRANS_Y]
        return point
    }

    private fun Matrix.getScale(): Float {
        val values = FloatArray(9).apply { getValues(this) }
        val scaleX: Float = values[Matrix.MSCALE_X]
        val skewY: Float = values[Matrix.MSKEW_Y]
        return sqrt(scaleX.toDouble().pow(2.0) + skewY.toDouble().pow(2.0)).toFloat()
    }

    private val PhotoView.baseScale: Float
        get() = displayScale / suppScale

    private val PhotoView.suppScale: Float
        get() = scale

    private val PhotoView.displayScale: Float
        get() = Matrix().apply { getDisplayMatrix(this) }.getScale()
}